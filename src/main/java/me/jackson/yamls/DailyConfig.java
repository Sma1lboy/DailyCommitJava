package me.jackson.yamls;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Date;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/21
 */
public class DailyConfig {

    private Date lastUpdate;
    public DailyConfig() {

    }

    public DailyConfig(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
