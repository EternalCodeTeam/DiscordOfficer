package com.eternalcode.discordapp.user;

import com.j256.ormlite.dao.Dao;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

    CompletableFuture<Optional<User>> findUser(Long id);

    CompletableFuture<Dao.CreateOrUpdateStatus> saveUser(User user);

    CompletableFuture<Integer> deleteUser(User user);

    CompletableFuture<List<User>> selectAllUsers();

    CompletableFuture<Integer> deleteUserById(Long id);
}
