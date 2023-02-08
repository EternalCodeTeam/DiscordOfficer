package com.eternalcode.discordapp.database.repository.user;

import com.eternalcode.discordapp.database.model.User;
import com.eternalcode.discordapp.database.wrapper.UserWrapper;
import com.j256.ormlite.dao.Dao;
import panda.std.reactive.Completable;

import java.util.List;

public interface UserRepository {

    Completable<User> findUser(Long id);

    Completable<Dao.CreateOrUpdateStatus> saveUser(User user);

    Completable<Integer> deleteUser(User user);

    Completable<List<User>> selectAllUsers();

    Completable<Integer> deleteUserById(Long id);
}
