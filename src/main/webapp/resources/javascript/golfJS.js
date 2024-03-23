function tabNextScore(inputElement)
{
	try
	{
		var elementID = inputElement.name;
		
		var rowIndex1 = elementID.charAt(25);
		var rowIndex2 = elementID.charAt(26);
			
		var rowIndex = "";
		
		if (rowIndex2 === ":")
		{
			rowIndex = rowIndex1;
		}
		else
		{
			rowIndex = rowIndex1.concat(rowIndex2);			
		}	
		
		var newElementName = "";
		
		if (elementID.endsWith("inputHole1ID_input")) 
		{
			newElementName = elementID.replace("Hole1ID", "Hole2ID");			
		}
		else if (elementID.endsWith("inputHole2ID_input")) 
		{
			newElementName = elementID.replace("Hole2ID", "Hole3ID");			
		}
		else if (elementID.endsWith("inputHole3ID_input")) 
		{
			newElementName = elementID.replace("Hole3ID", "Hole4ID");			
		}
		else if (elementID.endsWith("inputHole4ID_input")) 
		{
			newElementName = elementID.replace("Hole4ID", "Hole5ID");			
		}
		else if (elementID.endsWith("inputHole5ID_input")) 
		{
			newElementName = elementID.replace("Hole5ID", "Hole6ID");			
		}
		else if (elementID.endsWith("inputHole6ID_input")) 
		{
			newElementName = elementID.replace("Hole6ID", "Hole7ID");			
		}
		else if (elementID.endsWith("inputHole7ID_input")) 
		{
			newElementName = elementID.replace("Hole7ID", "Hole8ID");			
		}
		else if (elementID.endsWith("inputHole8ID_input")) 
		{
			newElementName = elementID.replace("Hole8ID", "Hole9ID");			
		}
		else if (elementID.endsWith("inputHole9ID_input")) 
		{
			newElementName = elementID.replace("Hole9ID", "Hole10ID");			
		}
		else if (elementID.endsWith("inputHole10ID_input")) 
		{
			newElementName = elementID.replace("Hole10ID", "Hole11ID");			
		}
		else if (elementID.endsWith("inputHole11ID_input")) 
		{
			newElementName = elementID.replace("Hole11ID", "Hole12ID");			
		}
		else if (elementID.endsWith("inputHole12ID_input")) 
		{
			newElementName = elementID.replace("Hole12ID", "Hole13ID");			
		}
		else if (elementID.endsWith("inputHole13ID_input")) 
		{
			newElementName = elementID.replace("Hole13ID", "Hole14ID");			
		}
		else if (elementID.endsWith("inputHole14ID_input")) 
		{
			newElementName = elementID.replace("Hole14ID", "Hole15ID");			
		}
		else if (elementID.endsWith("inputHole15ID_input")) 
		{
			newElementName = elementID.replace("Hole15ID", "Hole16ID");			
		}
		else if (elementID.endsWith("inputHole16ID_input")) 
		{
			newElementName = elementID.replace("Hole16ID", "Hole17ID");			
		}
		else if (elementID.endsWith("inputHole17ID_input")) 
		{
			newElementName = elementID.replace("Hole17ID", "Hole18ID");			
		}		
		
		//no tabbing if we're on hole 18
		if (!elementID.endsWith("inputHole18ID_input")) 
		{
			var newElement = document.getElementById(newElementName);
			newElement.focus();	
		}		
		
		updateScores(rowIndex);		
	}
	catch (err)
	{
		alert(err);
	}
}	

function updateScores(rowIndex)
{
	var hole1ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole1ID_input";
	var hole2ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole2ID_input";
	var hole3ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole3ID_input";
	var hole4ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole4ID_input";
	var hole5ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole5ID_input";
	var hole6ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole6ID_input";
	var hole7ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole7ID_input";
	var hole8ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole8ID_input";
	var hole9ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole9ID_input";
	var hole10ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole10ID_input";
	var hole11ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole11ID_input";
	var hole12ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole12ID_input";
	var hole13ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole13ID_input";
	var hole14ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole14ID_input";
	var hole15ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole15ID_input";
	var hole16ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole16ID_input";
	var hole17ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole17ID_input";
	var hole18ElementName = "scoresForm:roundsTableID:" + rowIndex + ":inputHole18ID_input";

	var hole1Score = document.getElementById(hole1ElementName).value;
	var hole2Score = document.getElementById(hole2ElementName).value;
	var hole3Score = document.getElementById(hole3ElementName).value;
	var hole4Score = document.getElementById(hole4ElementName).value;
	var hole5Score = document.getElementById(hole5ElementName).value;
	var hole6Score = document.getElementById(hole6ElementName).value;
	var hole7Score = document.getElementById(hole7ElementName).value;
	var hole8Score = document.getElementById(hole8ElementName).value;
	var hole9Score = document.getElementById(hole9ElementName).value;
	var hole10Score = document.getElementById(hole10ElementName).value;
	var hole11Score = document.getElementById(hole11ElementName).value;
	var hole12Score = document.getElementById(hole12ElementName).value;
	var hole13Score = document.getElementById(hole13ElementName).value;
	var hole14Score = document.getElementById(hole14ElementName).value;
	var hole15Score = document.getElementById(hole15ElementName).value;
	var hole16Score = document.getElementById(hole16ElementName).value;
	var hole17Score = document.getElementById(hole17ElementName).value;
	var hole18Score = document.getElementById(hole18ElementName).value;
	
	var front9Total = 0;	

	if (!(isNaN(hole1Score)))
	{
		front9Total = front9Total + parseInt(hole1Score,10);
	}
	
	if (!(isNaN(hole2Score)))
	{
		front9Total = front9Total + parseInt(hole2Score,10);
	}
	
	if (!(isNaN(hole3Score)))
	{
		front9Total = front9Total + parseInt(hole3Score,10);
	}
	
	if (!(isNaN(hole4Score)))
	{
		front9Total = front9Total + parseInt(hole4Score,10);
	}
	
	if (!(isNaN(hole5Score)))
	{
		front9Total = front9Total + parseInt(hole5Score,10);
	}
	
	if (!(isNaN(hole6Score)))
	{
		front9Total = front9Total + parseInt(hole6Score,10);
	}
	
	if (!(isNaN(hole7Score)))
	{
		front9Total = front9Total + parseInt(hole7Score,10);
	}
	
	if (!(isNaN(hole8Score)))
	{
		front9Total = front9Total + parseInt(hole8Score,10);
	}
	
	if (!(isNaN(hole9Score)))
	{
		front9Total = front9Total + parseInt(hole9Score,10);
	}
	
	var back9Total = 0;
	
	if (!(isNaN(hole10Score)))
	{
		back9Total = back9Total + parseInt(hole10Score,10);
	}
	
	if (!(isNaN(hole11Score)))
	{
		back9Total = back9Total + parseInt(hole11Score,10);
	}
	
	if (!(isNaN(hole12Score)))
	{
		back9Total = back9Total + parseInt(hole12Score,10);
	}
	
	if (!(isNaN(hole13Score)))
	{
		back9Total = back9Total + parseInt(hole13Score,10);
	}
	
	if (!(isNaN(hole14Score)))
	{
		back9Total = back9Total + parseInt(hole14Score,10);
	}
	
	if (!(isNaN(hole15Score)))
	{
		back9Total = back9Total + parseInt(hole15Score,10);
	}
	
	if (!(isNaN(hole16Score)))
	{
		back9Total = back9Total + parseInt(hole16Score,10);
	}
	
	if (!(isNaN(hole17Score)))
	{
		back9Total = back9Total + parseInt(hole17Score,10);
	}
	
	if (!(isNaN(hole18Score)))
	{
		back9Total = back9Total + parseInt(hole18Score,10);
	}	
	
	var totalScore = front9Total + back9Total;	
	
	var front9ElementName = "scoresForm:roundsTableID:"+rowIndex+":front9ID";
	var back9ElementName = "scoresForm:roundsTableID:"+rowIndex+":back9ID";
	var totalScoreElementName = "scoresForm:roundsTableID:"+rowIndex+":totalScoreID";
	
	//need innerHTML here because p:outputLabel renders a HTML <span> element with the value in its body. 
	//To alter the body of a <span> in JavaScript you need to manipulate the innerHTML.

	document.getElementById(front9ElementName).innerHTML = front9Total;
	document.getElementById(back9ElementName).innerHTML = back9Total;
	document.getElementById(totalScoreElementName).innerHTML = totalScore;	
}

function composePickListParms()
{
	var inputElement = document.getElementById("gameList");
	var elementValue = inputElement.value;
	var game = elementValue.options[e.selectedIndex];
	return game;
}