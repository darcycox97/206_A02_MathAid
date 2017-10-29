#!/bin/bash

# Script implementing a maths authoring aid, that makes creating "creations"
# much simpler for users of the script. Gives the user the option to play, list,
# or delete existing creations, and the main functionality of making new "creations".
# Author: Darcy Cox
# Date: 13/08/2017 

# defined functions below here...

# function to be called to halt script until user presses any key.
# takes either a single option identifying a common prompt,
# or a prompt defined outside this function which it will just print.
# options:
# "m" - Press any key to return the the menu: "
any_key() {

  local msg=""
  if [ "$1" == "m" ]
  then
    msg="Press any key to return to the menu: "
  else
    msg=$1
  fi

  read -n 1 -r -s -p "$msg"
  echo ""
}


# function to be called to get user to select yes or no
# selection is stored in global var "yesOrNo"
# displays a prompt specified by the first argument
yes_no() {

  local prompt="$1"

  while [ true ] # this loop exits when valid yes or no selection is given
  do
    read -p "$prompt" yesOrNo
    echo ""

    case $yesOrNo in
      [yY]|[yY][eE][sS]|[nN]|[nN][oO])
        break ;;
      *) 
         ;;
     esac

  done
}


# function to be called whenever user is to select a creation.
# takes one option as an argument.

# options:
# "l": only list creations
# "d": select creation and print messages relevant to deleting creations
# "p": select creation and print messages relevant to playing creations

# if "d" or "p" option given, selected creation is stored in the global variable "creation"
# this is the path to the creation, minus the file extension which we can added depending what
# part of the creation we are dealing with.

# exit code: 
# 0 if creations exist
# 1 if creations dir is empty, or user chooses to quit before selecting a creation
select_creation() {

  # get number of files in creations dir
  local numCreations=`ls creations 2> /dev/null| sort | wc -w`

  # check that creations dir exists and is non empty
  if [ -e creations ] && [ $numCreations -ne 0 ]
  then

    # list contents of folder by storing sorted output in an array and formatting each element
    local creationsInDir=(`ls creations | sort 2> /dev/null`)
    
    echo "The existing creations are....."
    echo ""

    local j=1
    for i in ${creationsInDir[@]}
    do
      echo "$j) $i"
      j=$((j+1))
    done

    echo ""
    
    # check if argument is not equal to "l"
    # if not, proceed to prompting user to select creation
    if [ "$1" != "l" ]
    then
    
      # determine what message to display to user
      local prompt=""
      if [ "$1" == "d" ]
      then
        prompt="Please enter the number of the creation you want to delete, or enter q to return to menu: "
      elif [ "$1" == "p" ]
      then
        prompt="Please enter the number of the creation you want to play, or enter q to return to menu: "
      fi
    
      # prompt user to select creation, keep prompting until they select a valid one.
      # if q is pressed, go back to menu
      local selection=0
      while [ true ]
      do
        read -p "$prompt" selection
        echo ""
        if ( [ $selection -gt 0 ] && [ $selection -le $numCreations ] ) 2> /dev/null
        then 
          break
        elif [ "$selection" == "q" ]
        then
          break
        else
          echo "Sorry, that number does not represent a creation."
          echo ""
        fi
      done
 
      if [ "$selection" == "q" ]
      then
        return 1
      else
        # use selection variable to extract the selected creation from the array
        # store this creaiton in global variable "creation"
        selection=$((selection-1))
        creation=${creationsInDir[$selection]}
        creation="creations/$creation/$creation"
      fi

    else
      any_key m
      echo ""
    fi

    return 0

  else  
    # creations folder is empty or doesnt exist, print message to user
    echo "You haven't made any creations yet, please choose another option."
    echo ""
    any_key m
    echo ""
    return 1
  fi
}
# end of defined functions


# Start main script...............
# print introduction message 
echo \
"==============================================================
Welcome to the Maths Authoring Aid
=============================================================="

#start loop
while [ true ]
do
echo -n \
"Please select from one of the following options:
(l)ist existing creations
(p)lay an existing creation
(d)elete an existing creation
(c)reate a new creation
(q)uit authoring tool

Enter a selection [l/p/d/c/q]:"

# prompt user to choose a selection
read userSelection
echo ""

case $userSelection in
  l) # list existing creations

    # call function with "l" option to only list creations.
    select_creation $userSelection
    ;;
		
  p) # play an existing creation
    
    while [ true ] # loop that exits once user is finished with play mode
    do
      # prompt user to select a creation, if any exist
      select_creation $userSelection 

      # proceed to play selected creaiton if any creations exist
      if [ $? -eq 0 ]
      then
        ffplay -window_title `basename $creation` -autoexit $creation-combined.mp4 &> /dev/null
        yes_no "Do you wish to play another creation? [y/n]: "

        case $yesOrNo in
          [yY]|[yY][eE][sS])
            ;;
          *)
            break
            ;;
        esac

      else
        break
      fi
    done
	;;

  d) # delete an existing creation

    while [ true ] # loop that exits once user is done with delete mode.
    do
      # prompt user to select creation, if any exist
      select_creation $userSelection
    
      # proceed if user has selected a valid creation
      if [ $? -eq 0 ]
      then
        # ask user if they are sure they want to delete
        yes_no "Are you sure you want to delete the creation \"`basename $creation`\" ? [y/n]: "

        case $yesOrNo in
          [yY]|[yY][eE][sS])
            rm $creation{-combined.mp4,.mp3,.mp4} &> /dev/null
            rmdir `dirname $creation` &> /dev/null
            echo "Creation \"`basename $creation`\" successfully deleted."
            echo ""
            ;;

           *)
             echo "Creation \"`basename $creation`\" not deleted."
             echo ""
            ;;
        esac

        yes_no "Do you wish to delete another creation? [y/n]: "
        
        case $yesOrNo in
          [yY]|[yY][eE][sS])
            ;;
          *)
            break
            ;;
        esac
      else
        break
      fi
    done
    ;;

  c) # create a new creation

    while [ true ] # loop that exits once user is done with create mode
    do
      # if creations folder does not yet exist, create it
      if [ ! -e creations ]
      then
        mkdir creations &> /dev/null
      fi

      # start loop that ends once a non-existing creation name has been given
      # or user chooses to overwrite existing creation
      while [ true ]
      do
        while [ true ] # this loop exits when user provides valid name (no spaces)
        do
          # prompt user for creation name
          echo "What would you like to name this creation?"
          read -p "Name: " creation
          echo ""

          case $creation in
            *\ *)
              echo "Please do not include any spaces in the creation name."
              echo "" ;;
            *) break ;;
          esac

        done

        # modify creation variable so it holds the path to the creation, minus the extension
        creation=creations/$creation/$creation

        # if this creation exists, ask the user if they wish to overwrite
        if [ -e `dirname $creation` ]
        then
          yes_no "That creation already exists. Do you want to overwrite it? [y/n]: "

        case $yesOrNo in
          [yY]|[yY][eE][sS])
            # remove all components so we can overwrite
            rm $creation{-combined.mp4,.mp3,.mp4} &> /dev/null
            break ;; # proceed to producing video
          *)
            ;; # prompt for a different creation name
        esac
    
        else 
          # create directory for creation
          dirname $creation | xargs mkdir &>/dev/null 
          break
        fi

      done # end of creation name loop. valid name has now been given
    
      # create video with blue background and centred text using the name of the creation
      ffmpeg -y -f lavfi -i color=c=blue -vf "drawtext=fontfile=:fontsize=30:fontcolor=white:\
      x=(w-text_w)/2:y=(h-text_h)/2:text='`basename $creation`'" -t 3 $creation.mp4 &> /dev/null

      # print message letting user know video has been created
      echo "Video component of creation \"`basename $creation`\" successfully created."
      echo ""

      echo "You may now record the audio for this creation."
      echo ""

      # while loop that exits when user chooses to keep the recording
      while [ true ]
      do
        any_key "Press any key to start recording. Recording will last 3 seconds: "
        echo ""

        echo -n "Recording......................."
        ffmpeg -f alsa -ac 2 -i default -t 3 $creation.wav &> /dev/null                
      #  arecord -f cd -d 3 $creation.wav &> /dev/null # alternative recording command to try if ^ does not work on the lab machines
        echo ""
        echo ""
        echo "Finished recording!"
        echo ""

        # convert wav to mp3, then remove the wav
        ffmpeg -i $creation.wav -f mp3 $creation.mp3 &> /dev/null
        rm $creation.wav &> /dev/null

        # ask user if they wish to hear the recording
        yes_no "Do you want to listen to the recording? [y/n]: "
        case $yesOrNo in
          [yY]|[yY][eE][sS])
            # play back audio and ask the user to (k)eep or (r)edo
            ffplay -window_title `basename $creation` -autoexit $creation.mp3 &> /dev/null

            while [ true ] # loop that exits when k or r is selected
            do
              read -p "Would you like to (k)eep this recording, or (r)edo it? [k/r]: " selection
              echo ""
        
              case $selection in
                [kK]|[rR]) 
                  break ;;
                *) ;;
              esac
            done # k or r is selected

            case $selection in
            [kK]) 
              break ;;
            *) 
              rm $creation.mp3 &> /dev/null
              ;;
            esac
             ;;

        *)
          break ;;
       esac
      done # user has chosen to keep recording

      # let user know audio component has been created
      echo "Recording saved!"
      echo ""

      # combine the video and audio files
      ffmpeg -i $creation.mp4 -i $creation.mp3 -codec copy $creation-combined.mp4 &> /dev/null

      # print success message
      echo "Creation \"`basename $creation`\" successfully created."
      echo ""

      # ask user if they wish to exit creation mode
      yes_no "Do you wish to create another creation? [y/n]: "
      case $yesOrNo in
      [yY]|[yY][eE][sS])
        ;;
      *)
        break
        ;;
      esac
    done # end of creation mode loop
    ;;

  q) # quit
    break;;

  *) # non-valid option

    echo "Sorry, your selection was not valid. Please try again with a valid selection."
    echo ""
    any_key m
    echo ""
    ;;
		
esac

done # end of main loop user has chosen to quit
exit 0

