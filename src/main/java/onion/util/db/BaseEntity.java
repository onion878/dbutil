package onion.util.db;

import onion.util.db.ibatis.SqlGenerate;

import java.util.List;

public abstract class BaseEntity {

    public int save() throws Exception {
        return SqlGenerate.createEntity(this);
    }

    public int update() throws Exception {
        return SqlGenerate.updateEntity(this);
    }

    public List query() throws Exception {
        return SqlGenerate.queryByEntity(this);
    }

    public int delete() throws Exception {
        return SqlGenerate.deleteEntity(this);
    }

    public int saveOrUpdate() throws Exception {
        if (SqlGenerate.getEntityById(this) == null) {
            return this.save();
        } else {
            return this.update();
        }
    }

}


