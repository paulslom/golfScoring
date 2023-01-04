package com.pas.dao;

import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.Group;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupRowMapper implements RowMapper<Group>, Serializable 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Group mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
        Group group = new Group();
        
        group.setGroupID(rs.getInt("idgroup"));
		group.setGroupName(rs.getString("groupName"));

        return group;
    }
}
