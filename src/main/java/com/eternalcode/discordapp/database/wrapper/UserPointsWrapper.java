package com.eternalcode.discordapp.database.wrapper;

import com.eternalcode.discordapp.database.model.UserPoints;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigInteger;
import java.util.Collection;

@DatabaseTable(tableName = "edc_user_points")
public class UserPointsWrapper {

        @DatabaseField(id = true)
        private Long id;

        @DatabaseField(columnName = "points", defaultValue = "0")
        private int points;

        public UserPointsWrapper() {
        }

        public UserPointsWrapper(Long id, int points) {
            this.id = id;
            this.points = points;
        }

        public static UserPointsWrapper from(UserPoints userPoints) {
            return new UserPointsWrapper(userPoints.getId(), userPoints.getPoints());
        }

        public UserPoints toUserPoints() {
            return new UserPoints(this.id, this.points);
        }
}
