package com.pas.beans;

import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;

@Named("pc_Group")
@SessionScoped
public class Group extends SpringBeanAutowiringSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(Game.class);

	private Integer groupID;
	private String groupName;
	private Group selectedGroup;
			
	public void valueChangeGroup(AjaxBehaviorEvent event) 
	{
		log.info("user selected a golf Group from main page");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Group selectedGroup = (Group)selectonemenu.getValue();
		
		if (selectedGroup != null)
		{
			log.info("loading up golf courses from MySQL DB golfScoring");	 
			GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
			
			gm.loadCourseSelections();			
			
			gm.setDisableProceedToSelectGame(false);			
		}
		
	}
	@Override
    public boolean equals(final Object o) 
	{
        if (this == o) 
        {
            return true;
        }
        if (!(o instanceof Integer)) 
        {
            return false;
        }
        
        final Integer that = (Integer) o;
        return Objects.equals(groupID, that);
    }
	
	public Group getSelectedGroup() {
		return selectedGroup;
	}
	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
	public Integer getGroupID() {
		return groupID;
	}
	public void setGroupID(Integer groupID) {
		this.groupID = groupID;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
}
