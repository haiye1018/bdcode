#!/bin/sh

	# Declare a string variable with escaped double quotation marks
	str2="Hello this ATA  \"Blog Site\""
	# Prints the string value from str2 variable
	echo $str2

	# Declare a variable named owner with a string value
	owner="Adam"
	# Declare a string variable with escaped double quotation marks 
	# and a substituted value from the owner variable
	str3="Hello this ATA \"Blog Site\" by $owner"
	# Prints the string value from str3 variable 
	echo $str3

	ls -al /
	if [ $? -eq 0 ] # Compare if the exit status of the previous command equates to 0
	then
		# If the exit status is 0, then print this message below
	  echo "Successfully"
	else
	  # If the exit status is non-zero, then print this message below
	  echo "Failed"
	fi


	function newfunction()
	{
	 echo "My First Name is $1"
	 echo "My Last Name is $2"
	}
	newfunction Adam Candy

	for filename in * # Scans all files (*) in the current directory
	do
		echo $filename
	done


	a=100 # Declaring initial value in a variable
	# Sets condition to run the commands inside the while loop 
	# while the value is less than 110
	while [ $a -lt 110 ] 
	do
	   echo $a # Prints the current value
	   a=`expr $a + 2`# Increments the value by two
	done
	
	# 管道
	wc -l /hadoop/config.conf
	cat /hadoop/config.conf | wc -l
	cat /hadoop/config.conf | awk -F ' ' '{print $2}'
	cat /hadoop/config.conf | awk -F ' ' '{print $2}' | awk -F '=' '{print $1}' | sed 's/_IS_INSTALL//'
