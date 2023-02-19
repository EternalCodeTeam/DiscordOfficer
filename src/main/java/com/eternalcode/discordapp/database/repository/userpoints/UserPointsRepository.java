package com.eternalcode.discordapp.database.repository.userpoints;

import com.eternalcode.discordapp.database.model.UserPoints;
import com.j256.ormlite.dao.Dao;
import panda.std.reactive.Completable;

import java.util.List;

interface UserPointsRepository {
    Completable<UserPoints> findUser(Long id);

    Completable<Dao.CreateOrUpdateStatus> saveUser(UserPoints user);

    Completable<Integer> deleteUser(UserPoints user);

    Completable<Integer> deleteUserById(Long id);
}
