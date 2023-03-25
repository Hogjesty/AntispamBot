package org.example.dao;

import org.example.dtos.UserDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BannedMembersDaoImpl implements BannedMembersDao {

    private final Connection connection;

    public BannedMembersDaoImpl() {
        this.connection = Connect.getInstance();
    }

    @Override
    public void insert(UserDto dto) {
        try {
            PreparedStatement ps = connection.prepareStatement("insert into telegram_schema.banned_members (ban_date, id, username) values (now(), ?, ?);");
            ps.setLong(1, dto.getUserId());
            ps.setString(2, dto.getUsername());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> res = new ArrayList<>();
        try {
            ResultSet set = connection.createStatement().executeQuery("select * from telegram_schema.banned_members;");

            while (set.next()) {
                res.add(UserDto.builder()
                        .userId(set.getLong(1))
                        .username(set.getString(3))
                        .build());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public void delete(Long id) {
        try {
            PreparedStatement preparedStatement = connection
                    .prepareStatement("delete from telegram_schema.banned_members where id = ?;");

            preparedStatement.setLong(1, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
