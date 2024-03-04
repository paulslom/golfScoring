package com.pas.beans;

import java.io.Serializable;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.springframework.stereotype.Component;

import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;
import com.pas.util.BeanUtilJSF;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

@Named("pc_Group")
@Component
public class Group implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(Game.class);

	private String groupID;
	private Integer oldGroupID;
	private String groupName;
	private Group selectedGroup;
			
	public void valueChangeGroup(AjaxBehaviorEvent event) 
	{
		logger.info("user selected a golf Group from main page");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Group selectedGroup = (Group)selectonemenu.getValue();
		
		try 
		{
			if (selectedGroup != null)
			{
				logger.info("loading up golf courses");	 
				GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
				DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
				gm.loadCourseSelections(dynamoClients);				
				gm.setDisableProceedToSelectGame(false);			
			}
		} 
		catch (Exception e) 
		{
			logger.error("Exception in Group valueChangeGroup: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in Group valueChangeGroup: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
	
		}			
		
		
	}
	@Override
    public boolean equals(final Object o) 
	{
        if (this == o) 
        {
            return true;
        }
        if (!(o instanceof String)) 
        {
            return false;
        }
        
        final String that = (String) o;
        return Objects.equals(groupID, that);
    }
	
	public Group getSelectedGroup() {
		return selectedGroup;
	}
	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public Integer getOldGroupID() {
		return oldGroupID;
	}
	public void setOldGroupID(Integer oldGroupID) {
		this.oldGroupID = oldGroupID;
	}
	
}
