package com.vile.input;

import java.util.ArrayList;

import com.vile.Display;
import com.vile.Game;
import com.vile.PopUp;
import com.vile.SoundController;
import com.vile.entities.Button;
import com.vile.entities.Door;
import com.vile.entities.Elevator;
import com.vile.entities.Entity;
import com.vile.entities.Explosion;
import com.vile.entities.HurtingBlock;
import com.vile.entities.Item;
import com.vile.entities.ItemNames;
import com.vile.entities.Player;
import com.vile.graphics.Render3D;
import com.vile.launcher.FPSLauncher;
import com.vile.levelGenerator.Block;
import com.vile.levelGenerator.Level;

/**
 * Title: Controller
 * @author Alex Byrd
 * Date Updated: 5/01/2017
 *
 * Takes all the input from the controls sent from game, and performs all
 * the actions of the game and helps set the variables used by Render3D
 * to render the 3D environment of the game correctly as the Player moves.
 * 
 * Also it updates movement, and detects collision.
 */
public class Controller 
{
   /*
    * Position and movement variables.
    */
	private double xa;
	private double za;
	private double upRotationa;
	private double rotationa;
	
   /*
    * Variables used to check certain cases, so that some actions do not
    * activate if another one is activated. For instance, if your in a 
    * jump you cannot jump again, and etc...
    */
	private boolean inJump            = false;
	private boolean crouching         = false;
	
	//If crouching while falling, only increase damage once
	private boolean once              = false;
	
   /*
    * Handles time between when you can activate certain events
    */
	private int     time;
	private int     time2;
	
   /*
    * Variables used in falling acceleration (gravity)
    */
	private double  fallSpeed         = 0.4;
	private double  fallAmount        = 0;
	
	//Public and static because it can be changed when accessing a 
	//new custom resource pack. (In case on the moon or other planet)
	public static  double  acceleration      = 0.03;

   /*
    * Other variables
    */
	public static boolean mouseLeft      = false;
	public static boolean mouseRight     = false;
	public static boolean mouseUp        = false;
	public static boolean mouseDown      = false;
	public static boolean showFPS        = false;
	public static boolean quitGame       = false;
	public static double moveSpeed       = 1.0;
	public static double rotationUpSpeed = 0.01;
	public static int 	 timeAfterShot;
	public static boolean shot        = false;
	
	//Whether players starting height has been checked yet
	private boolean firstCheck = false;
	
	//How long has it been since the player has last tried
	//to activate something
	private int useTime = 0;
	
	
   /**
    * Takes all the values in from reading the keyboard, and
    * activates certain things depending on what values are true
    * or not. This method also the x, z, and rotation speeds so that
    * they can be used by the Render3D method to move the player 
    * the corresponding ways.
    * 
    * Also takes care of shooting, flying, and other such events.
    */
	public void performActions(Game game)
	{
	   /*
	    * Reset these variables each time it runs through.
	    */
		double rotationSpeed = 0.03;
		double horizontalMouseSpeed = 0.005 * Display.mouseSpeedHorizontal;
		double verticalMouseSpeed = 0.001 * Display.mouseSpeedVertical;
		
		//Amount your moving in x or z direction each time it loops
		double moveX         = 0;
		double moveZ         = 0;
		
	   /*
	    * Only allow these actions if the player is alive
	    */
		if(Player.alive)
		{
		   /*
		    * If you shoot, then set shot to true
		    */
			if(Game.shoot)
			{
				shot = true;
			}
			
			//If moving forward
			if(Game.foward)
			{
				moveZ += 1/21.3;
			}
		
			//If moving back
			if(Game.back)
			{
				moveZ -= 1/21.3;
			}
		
			//If strafing left
			if(Game.left)
			{
				moveX -= 1/21.3;
			}
		
			//If strafing right
			if(Game.right)
			{
				moveX += 1/21.3;
			}
		
			//If turning left
			if(Game.turnLeft)
			{
				rotationa -= rotationSpeed;
			}
		
			//If turning right
			if(Game.turnRight)
			{
				rotationa += rotationSpeed;
			}
			
			//If looking up
			if(Game.turnUp)
			{
				upRotationa -= rotationUpSpeed;
			}
		
			//If looking down
			if(Game.turnDown)
			{
				upRotationa += rotationUpSpeed;
			}
		
			//If mouse goes left
			if(mouseLeft)
			{
				rotationa += horizontalMouseSpeed;
				
				//if(rotationa < -0.6)
				//{
					//rotationa = 0;
				//}
				
				mouseLeft = false;
			}
		
			//If mouse moves right
			if(mouseRight)
			{
				rotationa += horizontalMouseSpeed;
				
				//if(rotationa > 0.6)
				//{
					//rotationa = 0;
				//}
				
				mouseRight = false;
			}
			
			//If mouse moves up
			if(mouseUp)
			{
				upRotationa += verticalMouseSpeed;
				
				//if(upRotationa < -0.1)
				//{
					//upRotationa = 0;
				//}
				
				mouseUp = false;
			}
		
			//If mouse moves down
			if(mouseDown)
			{
				upRotationa += verticalMouseSpeed;
				
				//if(upRotationa > 0.1)
				//{
					//upRotationa = 0;
				//}
				
				mouseDown = false;
			}
		
			//If running, set moveSpeed to 1.5 times the value it was
			if(Game.run)
			{
				moveSpeed *= 1.5;
			}
			
		   /*
		    * If you are pressing crouch, and not going up because of
		    * jumping. Halve the moveSpeed, set crouching = true, and keep
		    * performing a given operation while you are crouching.
		    */
			if(Game.crouch && !inJump)
			{	
				//If still not as low as you can go
				if(Player.yCorrect > -6.0 + Player.y) 
				{
					//If flying, just fly down until the player reaches the ground
					if(Player.flyOn && 
							Player.y >= Player.maxHeight 
							+ Player.extraHeight)
					{
						Player.y  -= 0.8;
					}
					//If falling from getting out of flyMode
					else if(fallAmount > 0)
					{
					   /*
					    * If you are falling, your not crouching, but
					    * you are increasing your speed downward due to
					    * becoming smaller, and so your speed increases.
					    */
						crouching   = false;
						fallSpeed  *= 1.005;
						
					   /*
					    * Only increase the acceleration once when falling
					    * and hunkering down while falling
					    */
						if(!once)
						{
							fallAmount *= 1.1;
							once = true;
						}
					}
					//If not falling, just crouch at the normal speed
					//Also you have to be at your normal height or
					//below it already.
					else	
					{
						moveSpeed = 0.5;
						crouching = true;
						
						Player.yCorrect  -= 0.5;
						Player.height -= 0.17;
					}
				}
				
			
				//If there is some decimal error, round to -6.0
				if(Player.yCorrect <= -6.0 + Player.y)
				{
					Player.yCorrect = -6.0 + Player.y;
					Player.height = 0;
				}
			}
		   /*
		    * If coming up from a crouch and you have not reached
		    * your normal height yet and there is not a block right
		    * above you that you can't raise through.
		    */
			else if(Player.yCorrect < Player.y &&
					Player.height < 2
					&& (Player.yCorrect + 4 < Player.blockOn.y
							&& Player.blockOn.y > Player.y
							|| Player.blockOn.y <= Player.y
							|| Player.y + 2 < Player.blockOn.y))
			{
				crouching = false;
				
				//Slowly raise up
				if(Player.yCorrect < Player.y)
				{
					Player.yCorrect += 0.5;
				}
				
				//Also raises player up
				if(Player.height < 2)
				{
					Player.height += 0.17;
				}
				
				if(Player.height >= 2)
				{
					Player.height = 2;
				}
				
				if(Player.yCorrect > Player.y)
				{
					Player.yCorrect = Player.y;
				}
			}
			//If no longer crouching
			else
			{
				crouching = false;
			}
			
			//TODO here2
		
		   /*
		    * If fly mode is not on, jump like normal
		    */
			if(!Player.flyOn)
			{		
				//System.out.println(Player.y+" : "+Player.maxHeight+" : "+Player.extraHeight);
			   /*
			    * If the Player is trying to jump, and the player is on
			    * the ground, then jump. 
			    */
				if(Game.jump && Player.y == Player.maxHeight + Player.extraHeight)
				{
					inJump = true;
					Player.jumpHeight = Player.totalJump + Player.maxHeight + Player.extraHeight;
				}
				
			   /*
			    * If you haven't reached your max jumpHeight yet then keep
			    * going up, but if you have, then stop going up.
			    */
				if(inJump && Player.y < Player.jumpHeight
						&& (Player.y + 0.4 < (Player.blockOn.y) 
								&& Player.blockOn.y > 0 
								|| Player.y >= Player.blockOn.y))
				{
					Player.y += 0.4;
				}
				else
				{
					inJump = false;
				}
				
				//If you are above the ceiling, get back to the ceiling - 0.1
				if(Player.y >= Render3D.ceilingDefaultHeight)
				{
					Player.y = Render3D.ceilingDefaultHeight - 0.1;
				}
				
				//If you're falling, accelerate down
				if(Player.y > 0 + Player.maxHeight + Player.extraHeight && !inJump)
				{
					Player.y -= fallSpeed;
					fallAmount -= fallSpeed;
					fallSpeed = fallSpeed + (fallSpeed * acceleration);
				}
			}
			else
			{
				//If in flymode just go upward
				if(Game.jump)
				{
					Player.y += 0.8;
				}
				
				//If you are above the ceiling, get back to the ceiling
				if(Player.y >= Render3D.ceilingDefaultHeight)
				{
					Player.y = Render3D.ceilingDefaultHeight;
				}
			}
		
			//If Player.y is negligibly close to his/her max height
			//then just set the player at being their maxHeight
			if(Player.y < 0.4 + Player.maxHeight + Player.extraHeight &&
					Player.y > -0.4 + Player.maxHeight + Player.extraHeight)
			{
				Player.y = 0 + Player.maxHeight + Player.extraHeight;
				inJump     = false;
				fallSpeed  = 0.4;
			}
			
		   /*
		    * If you fell so hard you went below the normal Player.y for
		    * crouching, then set the Player.y equal to -7 to
		    * not go below the map.
		    */
			if(Player.y <= -7 + Player.maxHeight + Player.extraHeight
					&& !crouching)
			{
				Player.y     = -7 + Player.maxHeight;
				inJump         = false;
				fallSpeed      = 0.4;
			}
			
		   /*
		    * If you are on the ground, calculate whether you are getting
		    * fall damage or not. If you havent fallen, then you recieve
		    * no damage.
		    */
			if(fallAmount > 0)
			{
				if(!Player.godModeOn && Player.immortality == 0)
				{
					Player.health -= (int) (5 * (fallAmount / 25));
				}
				
				fallAmount     = 0;
			}
			
			//Turns unlimited ammo on or off
			if(Game.unlimAmmo && time == 0)
			{
				if(Player.unlimitedAmmoOn)
				{
					Player.unlimitedAmmoOn = false;
				}
				else
				{
					Player.unlimitedAmmoOn = true;
				}
				
				time++;
			}
			
			//If gun shot
			if(shot)
			{
				//If shot by using the keyboard, shut it off here
				if(Game.shoot)
				{
					shot = false;
				}
				
				//See if weapon can fire
				if(!Player.weapons[Player.weaponEquipped].shoot())
				{
					//Only play sound if weapon can't fire and its out of ammo
					if(Player.weapons[Player.weaponEquipped].ammo <= 0)
					{
						SoundController.ammoOut.playAudioFile(0);
					}
				}
			}
			
			//If the player chooses the first weapon slot
			//Set that weapon as being equipped
			if(Game.weaponSlot0)
			{
				Player.weaponEquipped = 0;
			}
			
			//If Player chooses the second weapon slot
			//Set that weapon as being equipped
			if(Game.weaponSlot1 && Player.weapons[1].canBeEquipped)
			{
				Player.weaponEquipped = 1;
			}
			//If weapon has not be picked up yet
			else if(Game.weaponSlot1)
			{
				PopUp temp = new PopUp("You don't have this weapon yet!");
				
				//Only display the message if its not on screen yet
				boolean exists = false;
				
				for(PopUp p: Display.messages)
				{
					if(temp.text == p.text)
					{
						exists = true;
						break;
					}
				}
				
				//If Message does not exist yet
				if(!exists)
				{
					Display.messages.add(temp);
				}
			}
			
			//If Player chooses the second weapon slot
			//Set that weapon as being equipped
			if(Game.weaponSlot2 && Player.weapons[2].canBeEquipped)
			{
				Player.weaponEquipped = 2;
			}
			//If weapon has not be picked up yet
			else if(Game.weaponSlot2)
			{
				PopUp temp = new PopUp("You don't have this weapon yet!");
				
				//Only display the message if its not on screen yet
				boolean exists = false;
				
				for(PopUp p: Display.messages)
				{
					if(temp.text == p.text)
					{
						exists = true;
						break;
					}
				}
				
				//If Message does not exist yet
				if(!exists)
				{
					Display.messages.add(temp);
				}
			}
			
			//If Player chooses the third weapon slot
			//Set that weapon as being equipped
			if(Game.weaponSlot3 && Player.weapons[3].canBeEquipped)
			{
				Player.weaponEquipped = 3;
			}
			//If weapon has not be picked up yet
			else if(Game.weaponSlot3)
			{
				PopUp temp = new PopUp("You don't have this weapon yet!");
				
				//Only display the message if its not on screen yet
				boolean exists = false;
				
				for(PopUp p: Display.messages)
				{
					if(temp.text == p.text)
					{
						exists = true;
						break;
					}
				}
				
				//If Message does not exist yet
				if(!exists)
				{
					Display.messages.add(temp);
				}
			}
			
		   /*
		    * If you try to reload, and there are still cartridges with
		    * ammo in them, and some time has passed since the last
		    * reload, then reload.
		    */
			if(Game.reloading &&  time == 0)
			{
			    if(Player.weapons[Player.weaponEquipped].reload())
			    {
			    	time++;
			    	
			    	SoundController.reload.playAudioFile(0);
			    }
			}
		}
		else
		{
			//If dead the player just sits and rotates while also
			//being near the ground to simulate a fallen body
			rotationa += 0.001;
			Player.y = -6.0;		
		}
		
		//If time is not 0, add to it
		if(time != 0)
		{
			time++;
		}
		
		//If 25 ticks have passed, set time to 0
		if(time == 25)
		{
			time = 0;
		}
		
		//Do the same thing with time2 for shooting
		if(time2 != 0)
		{
			time2++;
		}
		
		//Allow shooting to be faster though, so 11 instead of 25 ticks
		if(time2 == 11)
		{
			time2 = 0;
		}
		
	   /*
	    * If you want to show or not show fps, and some time has passed
	    * since you last performed the action, then flip the status of
	    * showFPS, and then start the ticks again since this action was
	    * last performed.
	    */
		if(Game.fpsShow && time == 0)
		{
			if(showFPS)
			{
				showFPS = false;
			}
			else
			{
				showFPS = true;
			}
			
			time++;
		}
		
		//If you want to quit the game, set quitGame to true
		if(Game.pause)
		{
			//Game is paused
			Display.pauseGame =  true;
			
			//It will quit the "game" in terms of all the events the game
			//ticks through, not the app itself.
			quitGame = true;		
		}
		
	   /*
	    * If the player is trying to use an entity such as a button,
	    * elevator or door, if the player is within a small distance
	    * of that particular entity then activate that entity.
	    * 
	    * Each time check all of these entities in the game to see if
	    * the player is within range of them.
	    */
		if(Game.use && Player.alive)
		{
			//If nothing is activated, this will activate the oomf sound
			boolean anyActivated = false;
			
			//Activate button is button pressed
			if(Game.buttons.size() > 0)
			{
				for(int i = 0; i < Game.buttons.size(); i++)
				{
					Button button = Game.buttons.get(i);
					
					//Has to be in range of button
					if(Math.abs(button.getZ() - Player.z) <= 0.95
							&& Math.abs(button.getX() - Player.x) <= 0.95
							&& !button.pressed)
					{
						button.pressed = true;
						button.activated = true;
						anyActivated = true;
						
						SoundController.activated.playAudioFile(0);
						//Play button press sound
						SoundController.buttonPress.playAudioFile(0);
						
						//Reset useTime and start it again
						useTime = 0;
						useTime++;
					}
				}
			}
			
		   /*
		    * See if player activated a door. Player has to be within
		    * range of door, and have the appropriate key to activate
		    * a door. Otherwise show on screen that you need a certain
		    * key to activate.
		    */
			for(int i = 0; i < Game.doors.size(); i++)
			{
				Door door = Game.doors.get(i);
				
				if(Math.abs(door.getZ() - Player.z) <= 0.95
						&& Math.abs(door.getX() - Player.x) <= 0.95)
				{
					if(door.doorType == 0 ||
							door.doorType == 1 && Player.hasRedKey ||
							door.doorType == 2 && Player.hasBlueKey ||
							door.doorType == 3 && Player.hasGreenKey ||
							door.doorType == 4 && Player.hasYellowKey)
					{
						//If door doesn't have to be activated by a button
						if(door.itemActivationID == 0
								&& !door.activated)
						{
							if(!SoundController.keyUse.isStillActive())
							{
								SoundController.keyUse.playAudioFile(door.distanceFromPlayer + 10);
							}
							door.activated = true;
							anyActivated = true;
						}
						else if(door.itemActivationID > 0)
						{
							if(!SoundController.keyTry.isStillActive())
							{
								SoundController.keyTry.playAudioFile(door.distanceFromPlayer + 10);
							}
							
							PopUp temp = new PopUp("This door is opened elsewhere!");
							
							//Only display the message if its not on screen yet
							boolean exists = false;
							
							for(PopUp p: Display.messages)
							{
								if(temp.text == p.text)
								{
									exists = true;
									break;
								}
							}
							
							//If Message does not exist yet
							if(!exists)
							{
								Display.messages.add(temp);
							}
						}
					}
					else
					{
						if(door.doorType == 1 && !Player.hasRedKey)
						{
						   /*
						    * Displays that the player cannot open the
						    * door without the red key.
						    */
							PopUp temp = new PopUp("You need the Red Keycard!");
							
							//Only display the message if its not on screen yet
							boolean exists = false;
							
							for(PopUp p: Display.messages)
							{
								if(temp.text == p.text)
								{
									exists = true;
									break;
								}
							}
							
							//If Message does not exist yet
							if(!exists)
							{
								Display.messages.add(temp);
							}
							
							if(!SoundController.keyTry.isStillActive())
							{
								SoundController.keyTry.playAudioFile(door.distanceFromPlayer + 10);
							}
						}
						else if(door.doorType == 2 && !Player.hasBlueKey)
						{
						   /*
						    * Displays that the player cannot open the
						    * door without the blue key.
						    */
							PopUp temp = new PopUp("You need the Blue Keycard!");
							
							boolean exists = false;
							
							for(PopUp p: Display.messages)
							{
								if(temp.text == p.text)
								{
									exists = true;
									break;
								}
							}
							
							//If Message does not exist yet
							if(!exists)
							{
								Display.messages.add(temp);
							}
							
							if(!SoundController.keyTry.isStillActive())
							{
								SoundController.keyTry.playAudioFile(door.distanceFromPlayer + 10);
							}
						}
						else if(door.doorType == 3 && !Player.hasGreenKey)
						{
						   /*
						    * Displays that the player cannot open the
						    * door without the green key.
						    */
							PopUp temp = new PopUp("You need the Green Keycard!");
							
							boolean exists = false;
							
							for(PopUp p: Display.messages)
							{
								if(temp.text == p.text)
								{
									exists = true;
									break;
								}
							}
							
							//If Message does not exist yet
							if(!exists)
							{
								Display.messages.add(temp);
							}
							
							if(!SoundController.keyTry.isStillActive())
							{
								SoundController.keyTry.playAudioFile(door.distanceFromPlayer + 10);
							}
						}
						else
						{
						   /*
						    * Displays that the player cannot open the
						    * door without the yellow key.
						    */
							PopUp temp = new PopUp("You need the Yellow Keycard!");
							
							boolean exists = false;
							
							for(PopUp p: Display.messages)
							{
								if(temp.text == p.text)
								{
									exists = true;
									break;
								}
							}
							
							//If Message does not exist yet
							if(!exists)
							{
								Display.messages.add(temp);
							}
							
							if(!SoundController.keyTry.isStillActive())
							{
								SoundController.keyTry.playAudioFile(door.distanceFromPlayer + 10);
							}
						}
					}
				}
			}
			
		   /*
		    * See if player is in range to activate an
		    * elevator
		    */
			for(int i = 0; i < Game.elevators.size(); i++)
			{
				Elevator elevator = Game.elevators.get(i);
				
				if(Math.abs(elevator.getZ() - Player.z) <= 0.95
						&& Math.abs(elevator.getX() - Player.x) <= 0.95)
				{
					//If door doesn't have to be activated by a button
					if(elevator.itemActivationID == 0)
					{
						elevator.activated = true;
						anyActivated = true;
						
						//Reset useTime and start it again
						useTime = 0;
						useTime++;
					}
					else
					{
						PopUp temp = new PopUp("This elevator is activated elsewhere!");
						
						boolean exists = false;
						
						for(PopUp p: Display.messages)
						{
							if(temp.text == p.text)
							{
								exists = true;
								break;
							}
						}
						
						//If Message does not exist yet
						if(!exists)
						{
							Display.messages.add(temp);
						}
					}
				}
			}
			
			//If nothing was activated play oomf sound effect
			if(!anyActivated && useTime == 0)
			{
				SoundController.tryToUse.playAudioFile(0);
				
				//Begin the next waiting cycle to play sound again
				useTime++;
			}
			
			//Keep ticking the wait time until it reaches 21
			if(useTime < 21 && useTime > 0)
			{
				useTime++;
			}
			//At 21 reset it to 0
			else
			{
				useTime = 0;
			}
		}
	   /*
	    * If player is dead, allow player to respawn at beginning of the 
	    * map so he/she does not have to replay through the whole game,
	    * but start them out with their default values, kind of like in
	    * DOOM when you die. Usually if your skilled enough you can
	    * survive and continue on going.
	    */
		else if(Game.use && !Player.alive)
		{
			new Player();
			
			//If not survival, reload map
			if(FPSLauncher.gameMode == 0)
			{
				Display.messages = new ArrayList<PopUp>();
				game.loadNextMap(false, "");
			}
			else
			{
				Display.messages = new ArrayList<PopUp>();
				//If survival mode then restart survival
				game.display.restartSurvival();
			}
		}
		else
		{
			//If the player is not trying to use a wall, reset useTime
			useTime = 0;
		}
		
	   /*
		* If you want to noclip, and some time has passed
		* since you last performed the action, then flip the status of
		* noClip, and then start the ticks again since this action was
		* last performed.
		*/
		if(Game.noClip && time == 0)
		{
			time++;
			
			if(!Player.noClipOn)
			{
				Player.noClipOn = true;
			}
			else
			{
				Player.noClipOn = false;
			}
		}
		
		//Same as above just for godMode
		if(Game.godMode && time == 0)
		{
			time++;
			
			if(!Player.godModeOn)
			{
				Player.godModeOn = true;
			}
			else
			{
				Player.godModeOn = false;
			}
		}
		
	   /*
	    * Same as above, except for this time if flyMode is off
	    * reset the fallSpeed, and turn inJump (meaning you are currently
	    * jumping), and up (Means your going up in the jump) off, while
	    * also turning flyMode on. 
	    * 
	    * If flyMode is on, then turn it off, but then set the fallHeight
	    * to your current height indicated by Player.y to start your
	    * decent through the air. If you are on the moon, in order to
	    * decrease the damage you recieve when hitting the ground, it
	    * decreases the fallHeight you are at (even though technically
	    * you are at the same height). 
	    */
		if(Game.fly && time == 0)
		{
			if(!Player.flyOn)
			{
				fallSpeed = 0.4;
				Player.flyOn     = true;
				
				//No longer in a jump
				inJump    = false;		
			}
			else
			{
				Player.flyOn      = false;
				fallAmount = Player.y - Player.maxHeight;
			}
			
			time++;
		}
		
	   /*
	    * Same as other debug modes.
	    */
		if(Game.superSpeed && time == 0)
		{
			if(!Player.superSpeedOn)
			{
				Player.superSpeedOn = true;
			}
			else
			{
				Player.superSpeedOn = false;
			}
			
			time++;
		}
		
		//If super speed is on, multiply players speed by 4
		if(Player.superSpeedOn)
		{
			moveSpeed *= 4;
		}
		
		//Give player all weapons and ammo limits
		if(Game.restock)
		{
			Player.weapons[0].ammo = Player.weapons[0].ammoLimit;
			Player.weapons[0].canBeEquipped = true;
			Player.weapons[0].dualWield = true;
			Player.weapons[1].ammo = Player.weapons[1].ammoLimit;
			Player.weapons[1].canBeEquipped = true;
			Player.weapons[2].ammo = Player.weapons[2].ammoLimit;
			Player.weapons[2].canBeEquipped = true;
			Player.weapons[3].ammo = Player.weapons[3].ammoLimit;
			Player.weapons[3].canBeEquipped = true;
			
			//Display that weapons were given
			PopUp temp = new PopUp("All weapons given!");
			
			boolean exists = false;
			
			for(PopUp p: Display.messages)
			{
				if(temp.text == p.text)
				{
					exists = true;
					break;
				}
			}
			
			//If Message does not exist yet
			if(!exists)
			{
				Display.messages.add(temp);
			}
		}
			
	   /*
	    * Calculates the change of x and z depending on which direction
	    * the player is moving, and the players rotation. The players 
	    * rotation is used to determine how the graphics are rotated and
	    * drawn around the player using sin and cos to make a complete
	    * circle around the player.
	    */
		xa += ((moveX * Math.cos(Player.rotation)) +
				(moveZ * Math.sin(Player.rotation))) * moveSpeed;
		za += ((moveZ * Math.cos(Player.rotation)) -
				(moveX * Math.sin(Player.rotation))) * moveSpeed;
		
		double xEffects = 0;
		double zEffects = 0;
		double yEffects = 0;
		
		if(Player.xEffects > 0)
		{
			xEffects = 0.2;
		}
		else if(Player.xEffects < 0)
		{
			xEffects = -0.2;
		}
		
		if(Player.zEffects > 0)
		{
			zEffects = 0.2;
		}
		else if(Player.zEffects < 0)
		{
			zEffects = -0.2;
		}
		
		if(Player.yEffects > 0)
		{
			yEffects = 2;
		}
		else if(Player.yEffects < 0)
		{
			yEffects = -2;
		}
		
		Player.y += (yEffects);
		
	   /*
	    * These determine if the space to the side of the player
	    * is a solid block or not. If its not then move the player,
	    * if it is then don't execute movement in that direction.
	    * 
	    * If noclip is on, you can clip through walls, so you can
	    * do this anyway.
	    */
		if(isFree(Player.x + xa + (xEffects), Player.z) || Player.noClipOn)
		{
			Player.x += xa + (xEffects);
		}
		
	   /*
		* These determine if the space in front or back of the player
		* is a solid block or not. If its not then move the player,
		* if it is then don't execute movement in that direction.
		* 
		* If noclip is on, you can clip through walls, so you can
		* do this anyway.
		*/
		if(isFree(Player.x, Player.z + za + (zEffects)) || Player.noClipOn)
		{
			Player.z += za + (zEffects);
		}
		
		//Update player buffs (invincibility, etc...)
		Player.updateBuffs();

		try
		{
			//If it contains a Toxic Waste Block or Lava Block
			if(Game.hurtingBlocks.contains(Player.blockOn.wallEntity))
			{			
			   /*
			    * Hurt the player if the ticks have reset. Meaning player
			    * can only be hurt every so many ticks while on the block.
			    */
				Player.blockOn.wallEntity.activate();
				
				//Update HurtingBlock time only when the player is on the
				//block
				HurtingBlock.time++;
				
				if(HurtingBlock.time > 21 * Render3D.fpsCheck)
				{
					HurtingBlock.time = 0;
				}
			}
		}
		catch(Exception e)
		{
			
		}
		
		try
		{
			//If it is a line def
			if(Player.blockOn.wallItem.itemID == ItemNames.LINEDEF.itemID)
			{
				Item item = Player.blockOn.wallItem;
				
				//If End Level Line Def
				if(item.itemActivationID == 0)
				{
					Game.mapNum++;
					game.loadNextMap(false, "");
				}
				else
				{
					//Search through all the doors
					for(int k = 0; k < Game.doors.size(); k++)
					{
						Door door = Game.doors.get(k);
						
						//If door has the same activation ID as the 
						//button then activate it.
						if(door.itemActivationID 
								== item.itemActivationID)
						{
							door.activated = true;
						}
					}
					
					//Stores Items to be deleted
					ArrayList<Item> tempItems2 = new ArrayList<Item>();
					
					//Scan all activatable items
					for(int j = 0; j < Game.activatable.size(); j++)
					{
						Item itemAct = Game.activatable.get(j);
						
						//If Item is a Happiness Tower, activate it and
						//state that it is activated
						if(itemAct.itemID == ItemNames.RADAR.getID()
								&& !itemAct.activated &&
								item.itemActivationID ==
								itemAct.itemActivationID)
						{
							itemAct.activated = true;
							Display.messages.add(new PopUp("COM SYSTEM ACTIVATED"));
							SoundController.uplink.playAudioFile(0);
						}
						else
						{				
							//If item is enemy spawnpoint, then spawn the
							//enemy, and add the item to the arraylist of
							//items to be deleted
							if(itemAct.itemID == ItemNames.ENEMYSPAWN.getID()
									&& itemAct.itemActivationID 
									== item.itemActivationID)
							{
								Game.enemiesInMap++;
								game.addEnemy(itemAct.x, itemAct.z,
										itemAct.rotation);
								tempItems2.add(itemAct);
							}	
							//If Explosion has same activation ID of the button
							//then activate it
							else if(itemAct.itemID ==
									ItemNames.ACTIVATEEXP.getID()
									&& itemAct.itemActivationID 
									== item.itemActivationID)
							{
								new Explosion(itemAct.x, itemAct.y,
										itemAct.z, 0, 0);
								tempItems2.add(itemAct);
							}
							//If it gets rid of a wall, delete the wall and create an
							//air wall in its place.
							else if(itemAct.itemID 
									== ItemNames.WALLBEGONE.getID()
									&& itemAct.itemActivationID ==
									item.itemActivationID)
							{
								Block block2 = Level.getBlock
										((int)itemAct.x, (int)itemAct.z);
								
								//Block is effectively no longer there
								block2.height = 0;
								
								tempItems2.add(itemAct);
							}
						}
					}
					
					//Remove all the items that need to be deleted now
					for(int j = 0; j < tempItems2.size(); j++)
					{
						Item temp2 =  tempItems2.get(j);
								
						temp2.removeItem();
					}
				}
				
				//Play audio queue if there is one
				item.activateAudioQueue();
				
				//Remove linedef from game
				item.removeItem();
			}
		}
		catch(Exception e)
		{
			
		}
			
		try
		{
			//If a Secret block
			if(Player.blockOn.wallItem.itemID == ItemNames.SECRET.getID())
			{
			   /*
			    * Activate secret
			    */
				boolean activated = Player.blockOn.wallItem.activate();
				
			   /*
			    * If the item was activated remove it from the
			    * block, but not from the map so that it can
			    * still keep track of how many secrets you have
			    * found.
			    */
				if(activated)
				{
					Player.blockOn.wallItem = null;
				}
			}
		}
		catch(Exception e)
		{
			
		}
			
		try
		{
			//If a Teleporter enterance
			if(Player.blockOn.wallEntity.itemID 
					== ItemNames.TELEPORTERENTER.getID())
			{
				//Check all teleporters for a matching exit
			   for(int i = 0; i < Game.teleporters.size(); i++)
			   {
				   //Teleporter Object
				   Item tel = Game.teleporters.get(i);
				   
				   //If there is a teleporter exit with the same exact
				   //activation ID, teleport the player to that location
				   if(tel.itemActivationID ==
						   Player.blockOn.wallEntity.itemActivationID
						   && tel.itemID ==
						   ItemNames.TELEPORTEREXIT.getID())
				   {
					   Player.x = tel.x;
					   Player.z = tel.z;
					   
					   //Block teleporter exit is on
					   Block teleporterExit =
							   Level.getBlock((int)tel.x, (int)tel.z);
					   
					   //Set players y value to that new blocks height
					   Player.y = teleporterExit.height + teleporterExit.y;
					   
					   //Play teleportation sound
					   SoundController.teleportation.playAudioFile(0);
				   }
			   }
			}
		}
		catch(Exception e)
		{
			
		}

	   /*
	    * If the players y value + 4 is greater than the y of the
	    * block * 4(since the y of the block is corrected for
	    * rendering), then the player is either inside or on
	    * top of the block. The + 4 is there for stairs
	    */
		if(Player.y + 4 > (Player.blockOn.y * 4))
		{	
			Player.maxHeight = Player.blockOn.height + (Player.blockOn.y * 4)
					+ Player.blockOn.baseCorrect;
		}
		else
		{
			Player.maxHeight = 0;
		}
		
	   /*
	    * If player is on an item, add that items height to the
	    * current maxHeight
	    */
		if(Player.extraHeight > 0)
		{
			Player.maxHeight += Player.extraHeight;
			Player.extraHeight = 0;
		}

		//If not crouching or jumping or flying, and the player is
		//not dead then reset the players y value. Also the player
		//must not be partially crouched under a block (height has to
		//be two if fully standing up).
		if(!inJump && Player.y < Player.maxHeight
				&& Player.alive)
		{
			Player.y = Player.maxHeight;
		}
		
	   /*
	    * Only reset the players yCorrect if the player is effectively
	    * not crouching
	    */
		if(Player.height >= 2
				|| Player.yCorrect >= Player.y)
		{
			Player.yCorrect = Player.y;
		}
	   /*
	    * In case player is crouched and moving up stairs, this
	    * will make sure the crouched y value also changes.
	    */
		else if(Player.yCorrect + 6.0 < Player.y)
		{
			Player.yCorrect = Player.y - 6.0;
		}

		//TODO here

	   /*
	    * Causes the players movement to quickly (but not 
	    * instantaniously) move to 0 if there is not reset in
	    * the players movement.
	    */
		xa *= 0.1;
		za *= 0.1;
		
		//Increase the players rotation based on change in rotation
		Player.rotation += rotationa;
		
		//Decreases rotation steadily if you stop rotating
		rotationa = 0.0d;
		
		//Does the same things with upRotation
		Player.upRotate += upRotationa;
		
		upRotationa = 0.0d;
		
		//Sets lower bound on upRotation
		if(Player.upRotate >= 2.8)
		{
			Player.upRotate = 2.8;
		}
				
		//Sets upper bound on upRotation
		if(Player.upRotate <= 0.3)
		{
			Player.upRotate = 0.3;
		}		
	}
	
   /**
    * Is called with a given x and z value to determine if the block the
    * player is about to move to is free to move to or not. If it is
    * determined to be solid, you cannot move into it, and it stops your
    * movement, if not obviously you can move through it.
    * 
    * In terms of recent updates, if the block is higher than the players
    * position and height in the y direction, then the player can also
    * move under it. This is mainly used for doors, but will also come
    * in handy later for multi floored maps.
    * 
    * Since the 1.4 Update there are two methods, each is called 
    * individually depending on the direction the player is moving so
    * that at any height the walls will detect your collision correctly
    * no matter what the situation is. 
    * 
    * @param nextX
    * @param nextZ
    * @return
    */
	public boolean isFree(double nextX, double nextZ)
	{
		//Dont let player exit the map if not in noclip
		if(nextX < 0 || nextX > Level.width || nextZ < 0
				|| nextZ > Level.height)
		{
			return false;
		}
		
		// Number used for how far away from block, block detects
		double z = 0.25;
		
		//The current block the player is on
		Block block5 = Level.getBlock((int)
				(Player.x), (int)(Player.z));
		
	   /*
	    * Determine the block the Player is about to move into given the
	    * direction that it is going. Then set this block as the block
	    * to check the collision of. It actually does this to two blocks
	    * because it checks the distance on both sides of the player as
	    * he/she moves in a certain direction, because otherwise it'll
	    * only be checking to see if the player is "z" units from the 
	    * block on only one side, allowing the player to go through
	    * certain corners of blocks. This checks both sides to make sure 
	    * the player is not hitting a block.
	    */
		Block block = Level.getBlock((int)(nextX - z),(int)(nextZ - z));
		Block block2 = Level.getBlock((int)(nextX - z), (int)(nextZ + z));
			
		if(nextX < Player.x && nextZ == Player.z)
		{
			block = Level.getBlock((int)(nextX - z),(int)(nextZ - z));
			block2 = Level.getBlock((int)(nextX - z), (int)(nextZ + z));
		}
		else if(nextX >= Player.x && nextZ == Player.z)
		{
			block = Level.getBlock((int)(nextX + z),(int)(nextZ - z));
			block2 = Level.getBlock((int)(nextX + z), (int)(nextZ + z));
		}
		else if(nextX == Player.x && nextZ >= Player.z)
		{
			block = Level.getBlock((int)(nextX - z),(int)(nextZ + z));
			block2 = Level.getBlock((int)(nextX + z),(int)(nextZ + z));
		}
		else //(xx == Player.x && zz < Player.z)
		{
			block = Level.getBlock((int)(nextX - z),(int)(nextZ - z));
			block2 = Level.getBlock((int)(nextX + z),(int)(nextZ - z));
		}
		
		try
		{
			//If walking into block with a solid object on it
			if(block.wallItem.isSolid)
			{
				Item temp = block.wallItem;
				
				//Distance between item and player
				double distance = Math.sqrt(((Math.abs(temp.x - nextX))
						* (Math.abs(temp.x - nextX)))
						+ ((Math.abs(temp.z - nextZ))
								* (Math.abs(temp.z - nextZ))));
				
				//Difference in y
				double yDifference = Player.y - Math.abs(temp.y);
				
				//If close enough, don't allow the player to move into it.
				if(distance <= 0.3 && (yDifference <= temp.height
						&& yDifference >= -temp.height))
				{
					return false;
				}	
				//If on top of it, reset the players extra standing height
				else if(distance <= 0.3 && yDifference >= temp.height)
				{
					Player.extraHeight = temp.height + 5;
					return true;
				}	
			}
		}
		catch(Exception e)
		{
			
		}
		
		//Go through all the enemies in the game to make sure they are not
		//too close to you
		for(int i = 0; i < block.entitiesOnBlock.size(); i++)
		{
			Entity temp = block.entitiesOnBlock.get(i);
			
			//Distance between enemy and player
			double distance = Math.sqrt(((Math.abs(temp.xPos - nextX))
					* (Math.abs(temp.xPos - nextX)))
					+ ((Math.abs(temp.zPos - nextZ))
							* (Math.abs(temp.zPos - nextZ))));
			
			//Difference between enemy and player
			double yDifference = Player.y - Math.abs(temp.yPos);
			
			//If this enemy was removed from the game but not the block for
			//some reason, remove it now and allow the player to move.
			if(!Game.enemies.contains(temp))
			{
				block.entitiesOnBlock.remove(i);
				return true;
			}
			
			//If close enough, don't allow the player to move into it.
			if(distance <= 0.3 && yDifference <= 8
					&& yDifference >= -8)
			{
				return false;
			}	
			//Player can jump on top of enemies if height enough
			else if(distance <= 0.3 && yDifference >= 8)
			{
				//Reset players extra standing height
				Player.extraHeight = temp.height + 5;
				return true;
			}
			
			//If a boss, have the distance be farther the hitbox expands
			//out of
			if(distance <= 1 && temp.isABoss)
			{
				return false;
			}
		}
		
		Player.extraHeight = 0;
		
		//Adds the plus 4 to go up steps
		double yCorrect = Player.y + 4;
		
	   /*
	    * After first check, no longer set the players height as being
	    * the block that he/she is in. This is just to set the player
	    * up initially.
	    */
		if(!firstCheck)
		{
			Player.y = block5.height + (block5.y * 4) + block5.baseCorrect;
			Player.maxHeight = Player.y;
			firstCheck = true;
		}
		
	   /*
	    * Because the blocks Y in correlation to the players Y is 4 times
	    * less than what the players y would be at that height visually.
	    * (Ex. When a wall is at a y of 3, to the player it looks like it
	    * is at a y of 12, though it is not) This corrects it so the
	    * wall height being checked is the same height the player thinks
	    * it is. Do this for block2 as well
	    */
		double blockY = block.y * 4;
		double block2Y =  block2.y * 4;
		
		//If either block the player is moving into is solid
		if(block.isSolid || block2.isSolid)
		{
			//Make sure the player is not hitting either of the
			//blocks
			return (checkCollision(block, blockY, yCorrect)
					&& checkCollision(block2, block2Y, yCorrect));
		}
		
		return true;
	}
	
   /**
    * A private helper method that helps check to see if the Player can
    * move into the block that is sent in, and resets the Players
    * maximum standing height if he/she can to the blocks height.
    * 
    * @param block
    * @param blockY
    * @param yCorrect
    * @return
    */
	private boolean checkCollision(Block block, double blockY,
			double yCorrect)
	{
	   /*
	    * If player is between blocks bottom and top, don't let the player
	    * get on to, or go through the block
	    */
		if(block.height + (block.baseCorrect) + blockY > yCorrect
				&& yCorrect + Player.height >= blockY)
		{
			return false;
		}
		
		return true;
	}
}
