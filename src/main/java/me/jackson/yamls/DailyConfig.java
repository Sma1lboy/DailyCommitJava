package me.jackson.yamls;

import java.util.Date;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/21
 */
public class DailyConfig {

    private Date lastUpdate;

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
        return "Last Time Update is : " + lastUpdate.toString() + "\n";
    }
}
