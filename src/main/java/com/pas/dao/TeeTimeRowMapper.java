package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.TeeTime;

@Repository
public class TeeTimeRowMapper implements RowMapper<TeeTime>, Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public TeeTime mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
    	TeeTime teeTime = new TeeTime();
    		
		teeTime.setTeeTimeID(rs.getInt("idteeTimes"));
		teeTime.setGameID(rs.getInt("idgame"));
		teeTime.setPlayGroupNumber(rs.getInt("playGroupNumber"));
		teeTime.setTeeTimeString(rs.getString("teeTime"));
								 		
 		return teeTime;     	
    }
   
}
