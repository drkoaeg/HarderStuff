package com.draco18s.hardlib.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event.HasResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EntityAnimalInteractEvent extends LivingEvent {

	/**
	 * 
	 * @param entity
	 */
	public EntityAnimalInteractEvent(EntityLivingBase entity) {
		super(entity);
	}
	
	/**
     * AnimalLoveEvent is fired when an the player feeds wheat to an animal.<br>
     * This event is fired in EntityAnimal#func_146082_f(EntityPlayer) called from
     * EntityAniaml#interact(EntityPlayer) and the loveTime is how long the animal
     * will attempt to seek a mate.  Setting this to 0 will effectively cancel
     * love mode.<br>
     * The LivingEvent.entity is the entity calling the breed action.<br>
     * The AnimalLoveEvent.animalBred Class is the type of animal going into love mode.<br>
     * <br>
     * This event is fired via the {@link ForgeHooks#func_146082_f(EntityLivingBase)}.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
	public static class AnimalLoveEvent extends EntityAnimalInteractEvent {
		public int loveTime;
		public Class animalBred;
        public AnimalLoveEvent(EntityLivingBase e, Class<? extends EntityAnimal> b, int time){
        	super(e);
        	animalBred = b;
        	loveTime = time;
        }
    }
	
	/**
	 * The CowMilkEvent is fired when the player attempts to milk a cow or mooshroom.<br>
	 * If canceled, the player will not recieve any milk or stew.<br>
     * The LivingEvent.entity is the entity being milked.<br>
	 * <br>
	 * The event is {@link Cancelable}.<br>
	 * <br>
     * This event does not have a result. {@link HasResult}<br>
	 *
	 */
	@Cancelable
	public static class CowMilkEvent extends EntityAnimalInteractEvent {
		public CowMilkEvent(EntityCow entity) {
			super(entity);
		}
	}
}
