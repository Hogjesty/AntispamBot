package org.example.dao;

import org.example.dtos.UserDto;

import java.util.List;

public interface BannedMembersDao {
    void insert(UserDto user);

    List<UserDto> getAll();

    void delete(Long id);
}
