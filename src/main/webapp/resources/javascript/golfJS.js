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

function tabNextHole(inputElement)
{
	try
	{
		var elementID = inputElement.name;
		
		var newElementName = "";
		
		if (elementID.endsWith("hole1ParID")) 
		{
			newElementName = elementID.replace("hole1ParID", "hole2ParID");			
		}
		else if (elementID.endsWith("hole2ParID")) 
		{
			newElementName = elementID.replace("hole2ParID", "hole3ParID");				
		}
		else if (elementID.endsWith("hole3ParID")) 
		{
			newElementName = elementID.replace("hole3ParID", "hole4ParID");			
		}
		else if (elementID.endsWith("hole4ParID")) 
		{
			newElementName = elementID.replace("hole4ParID", "hole5ParID");				
		}
		else if (elementID.endsWith("hole5ParID")) 
		{
			newElementName = elementID.replace("hole5ParID", "hole6ParID");				
		}
		else if (elementID.endsWith("hole6ParID")) 
		{
			newElementName = elementID.replace("hole6ParID", "hole7ParID");				
		}
		else if (elementID.endsWith("hole7ParID")) 
		{
			newElementName = elementID.replace("hole7ParID", "hole8ParID");				
		}
		else if (elementID.endsWith("hole8ParID")) 
		{
			newElementName = elementID.replace("hole8ParID", "hole9ParID");			
		}
		else if (elementID.endsWith("hole9ParID")) 
		{
			newElementName = elementID.replace("hole9ParID", "hole10ParID");				
		}
		else if (elementID.endsWith("hole10ParID")) 
		{
			newElementName = elementID.replace("hole10ParID", "hole11ParID");				
		}
		else if (elementID.endsWith("hole11ParID")) 
		{
			newElementName = elementID.replace("hole11ParID", "hole12ParID");			
		}
		else if (elementID.endsWith("hole12ParID")) 
		{
			newElementName = elementID.replace("hole12ParID", "hole13ParID");				
		}
		else if (elementID.endsWith("hole13ParID")) 
		{
			newElementName = elementID.replace("hole13ParID", "hole14ParID");			
		}
		else if (elementID.endsWith("hole14ParID")) 
		{
			newElementName = elementID.replace("hole14ParID", "hole15ParID");			
		}
		else if (elementID.endsWith("hole15ParID")) 
		{
			newElementName = elementID.replace("hole15ParID", "hole16ParID");			
		}
		else if (elementID.endsWith("hole16ParID")) 
		{
			newElementName = elementID.replace("hole16ParID", "hole17ParID");			
		}
		else if (elementID.endsWith("hole17ParID")) 
		{
			newElementName = elementID.replace("hole17ParID", "hole18ParID");			
		}		
		
		//no tabbing if we're on hole 18
		if (!elementID.endsWith("hole18ParID")) 
		{
			var newElement = document.getElementById(newElementName);
			newElement.focus();	
		}
		
		updatePar();		
		
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

function updatePar()
{
	var hole1Par = document.getElementById('coursesForm:hole1ParID').value;
	var hole2Par = document.getElementById('coursesForm:hole2ParID').value;
	var hole3Par = document.getElementById('coursesForm:hole3ParID').value;
	var hole4Par = document.getElementById('coursesForm:hole4ParID').value;
	var hole5Par = document.getElementById('coursesForm:hole5ParID').value;
	var hole6Par = document.getElementById('coursesForm:hole6ParID').value;
	var hole7Par = document.getElementById('coursesForm:hole7ParID').value;
	var hole8Par = document.getElementById('coursesForm:hole8ParID').value;
	var hole9Par = document.getElementById('coursesForm:hole9ParID').value;
	var hole10Par = document.getElementById('coursesForm:hole10ParID').value;
	var hole11Par = document.getElementById('coursesForm:hole11ParID').value;
	var hole12Par = document.getElementById('coursesForm:hole12ParID').value;
	var hole13Par = document.getElementById('coursesForm:hole13ParID').value;
	var hole14Par = document.getElementById('coursesForm:hole14ParID').value;
	var hole15Par = document.getElementById('coursesForm:hole15ParID').value;
	var hole16Par = document.getElementById('coursesForm:hole16ParID').value;
	var hole17Par = document.getElementById('coursesForm:hole17ParID').value;
	var hole18Par = document.getElementById('coursesForm:hole18ParID').value;
	
	//alert('hole1Par = ' + hole1Par);
	//alert('hole2Par = ' + hole2Par);
	
	var front9Total = 0;	

	if (!(isNaN(hole1Par)))
	{
		front9Total = front9Total + parseInt(hole1Par,10);
	}
	
	//alert('2');
	
	if (!(isNaN(hole2Par)))
	{
		front9Total = front9Total + parseInt(hole2Par,10);
	}
	
	if (!(isNaN(hole3Par)))
	{
		front9Total = front9Total + parseInt(hole3Par,10);
	}
	
	if (!(isNaN(hole4Par)))
	{
		front9Total = front9Total + parseInt(hole4Par,10);
	}
	
	if (!(isNaN(hole5Par)))
	{
		front9Total = front9Total + parseInt(hole5Par,10);
	}
	
	if (!(isNaN(hole6Par)))
	{
		front9Total = front9Total + parseInt(hole6Par,10);
	}
	
	if (!(isNaN(hole7Par)))
	{
		front9Total = front9Total + parseInt(hole7Par,10);
	}
	
	if (!(isNaN(hole8Par)))
	{
		front9Total = front9Total + parseInt(hole8Par,10);
	}
	
	if (!(isNaN(hole9Par)))
	{
		front9Total = front9Total + parseInt(hole9Par,10);
	}
	
	var back9Total = 0;
	
	if (!(isNaN(hole10Par)))
	{
		back9Total = back9Total + parseInt(hole10Par,10);
	}
	
	if (!(isNaN(hole11Par)))
	{
		back9Total = back9Total + parseInt(hole11Par,10);
	}
	
	if (!(isNaN(hole12Par)))
	{
		back9Total = back9Total + parseInt(hole12Par,10);
	}
	
	if (!(isNaN(hole13Par)))
	{
		back9Total = back9Total + parseInt(hole13Par,10);
	}
	
	if (!(isNaN(hole14Par)))
	{
		back9Total = back9Total + parseInt(hole14Par,10);
	}
	
	if (!(isNaN(hole15Par)))
	{
		back9Total = back9Total + parseInt(hole15Par,10);
	}
	
	if (!(isNaN(hole16Par)))
	{
		back9Total = back9Total + parseInt(hole16Par,10);
	}
	
	if (!(isNaN(hole17Par)))
	{
		back9Total = back9Total + parseInt(hole17Par,10);
	}
	
	if (!(isNaN(hole18Par)))
	{
		back9Total = back9Total + parseInt(hole18Par,10);
	}	
	
	//alert('18');
	//alert('front 9 total= ' + front9Total);
	
	var totalPar = front9Total + back9Total;	
	
	var front9ElementName = "coursesForm:front9ParID";
	var back9ElementName = "coursesForm:back9ParID";
	var totalParElementName = "coursesForm:courseParID";
	
	//alert('22');
	//need innerHTML here because p:outputLabel renders a HTML <span> element with the value in its body. 
	//To alter the body of a <span> in JavaScript you need to manipulate the innerHTML.

	document.getElementById(front9ElementName).value = front9Total;
	document.getElementById(back9ElementName).value = back9Total;
	document.getElementById(totalParElementName).value = totalPar;	
}

function composePickListParms()
{
	var inputElement = document.getElementById("gameList");
	var elementValue = inputElement.value;
	var game = elementValue.options[e.selectedIndex];
	return game;
}