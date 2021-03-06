package com.draco18s.ores.entities;

import java.util.Random;

import com.draco18s.hardlib.api.interfaces.IMechanicalPowerUser;
import com.draco18s.ores.OresBase;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;

public class TileEntityWindmill extends TileEntity {
	public int coreX = 999;
	public int coreY = 999;
	public int coreZ = 999;

	private int millX = 999;
	private int millY = 999;
	private int millZ = 999;
	
	private int[][] lightArray;
	private boolean buildLight;
	private int powerAmt;

	public TileEntityWindmill() {
		lightArray = new int[10][5];
		buildLight = false;
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote) {
			//System.out.println(powerAmt + " " + powerScale(powerAmt));
		}
		else if(yCoord >= worldObj.provider.getAverageGroundLevel()-4 && coreX == xCoord && coreY == yCoord && coreZ == zCoord && millY != 999) {
			TileEntity tefromworld = worldObj.getTileEntity(millX, millY, millZ);
			if(tefromworld instanceof IMechanicalPowerUser) {
				IMechanicalPowerUser te = (IMechanicalPowerUser)tefromworld;
				//System.out.println("checking access");
				if(te != null) {
					if(!buildLight) {
						buildLight = true;
						checkAirVolumeFull(xCoord-millX,yCoord,zCoord-millZ);
					}
					int p = checkAirVolume(xCoord-millX,yCoord,zCoord-millZ);
					//System.out.println(p + " " + powerScale(p));
					if(powerAmt != p) {
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
						powerAmt = p;
					}
					if(p > te.minimumTorque()) {
						//te.setPowerStatus(true);
						te.setPowerLevel(te.powerScale(p - te.minimumTorque()));
					}
					else {
						//te.setPowerStatus(false);
						te.setPowerLevel(0);
					}
				}
			}
			else {
				millX = millY = millZ = 999;
			}
		}
	}

	private int checkAirVolume(int x, int y, int z) {
		if(worldObj.isRemote) return 750;
		x = (int) Math.signum(x);
		z = (int) Math.signum(z);
		Random rand = new Random();
		int i = rand.nextInt(10);
		int k = rand.nextInt(5);
		lightArray[i][k] = worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord+(i)*x+(k-2)*z, yCoord-2, zCoord+(i)*z+(k-2)*x);
		int lightTot = 0;
		int lowest = 99;
		for(i = 0; i < 10; ++i) {
			for(k = 0; k < 5; ++k) {
				lightTot += lightArray[i][k];
				if(lightArray[i][k] < lowest) {
					lowest = lightArray[i][k];
				}
			}	
		}
		
		return lightTot;
	}
	
	private void checkAirVolumeFull(int x, int y, int z) {
		if(worldObj.isRemote) return;
		x = (int) Math.signum(x);
		z = (int) Math.signum(z);
		for(int i = 0; i < 10; ++i) {
			for(int k = 0; k < 5; ++k) {
				lightArray[i][k] = worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord+(i)*x+(k-2)*z, yCoord-2, zCoord+(i)*z+(k-2)*x);
			}	
		}
	}

	public void recheckCore() {
		worldObj.scheduleBlockUpdate(coreX, coreY, coreZ, worldObj.getBlock(xCoord, yCoord, zCoord), 1);
	}

	public void setCore(int x, int y, int z) {
		coreX = x;
		coreY = y;
		coreZ = z;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 2);
	}

	public void setMill(int x, int y, int z) {
		millX = x;
		millY = y;
		millZ = z;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound tc) {
        super.readFromNBT(tc);
        coreX = tc.getInteger("coreX");
        coreY = tc.getInteger("coreY");
        coreZ = tc.getInteger("coreZ");
        millX = tc.getInteger("millX");
        millY = tc.getInteger("millY");
        millZ = tc.getInteger("millZ");
        powerAmt = tc.getInteger("airflow");
    }

    @Override
    public void writeToNBT(NBTTagCompound tc) {
        super.writeToNBT(tc);
        tc.setInteger("coreX", coreX);
        tc.setInteger("coreY", coreY);
        tc.setInteger("coreZ", coreZ);
        tc.setInteger("millX", millX);
        tc.setInteger("millY", millY);
        tc.setInteger("millZ", millZ);
        tc.setInteger("airflow", powerAmt);
    }
    
    public void invalidatePowerStatus() {
    	if(millY != 999) {
    		TileEntity te = worldObj.getTileEntity(millX, millY, millZ);
			if(te != null && te instanceof IMechanicalPowerUser) {
				((IMechanicalPowerUser)te).setPowerLevel(0);
				//te.setPowerStatus(false);
			}
    	}
    	if(coreY != 999 && !(coreX == xCoord && coreY == yCoord && coreZ == zCoord)) {
    		TileEntity te = worldObj.getTileEntity(coreX, coreY, coreZ);
			if(te != null && te instanceof TileEntityWindmill) {
				((TileEntityWindmill)te).recheckCore();
			}
    	}
    }

	public String getModelTexture() {
		if(this instanceof TileEntityWindvane) {
			return "ores:textures/entities/windvane.png";
		}
		if(getAxelPosition() == 3) {
			return "ores:textures/entities/gears.png";
		}
		return "ores:textures/entities/axel.png";
	}
	
	public float getOffsetX() {
		if(this instanceof TileEntityWindvane) {
			if(xCoord != coreX && coreY != 999) {
				return 0.25f;
			}
		}
		return 0.5f;
	}
	
	public float getOffsetY() {
		if(this instanceof TileEntityWindvane) {
			if(yCoord == coreY) {
				return 0;
			}
		}
		return -0.5f;
	}
	
	public float getOffsetZ() {
		if(this instanceof TileEntityWindvane) {
			if(xCoord != coreX) {
				return 0.5f;
			}
			if(xCoord == coreX && zCoord != coreZ) {
				return 0;
			}
			if(zCoord == millZ) {
				return 0.25f;
			}
		}
		return 0.5f;
	}
	
	public float getRotationX() {
		if(this instanceof TileEntityWindvane) {
			if(yCoord == coreY) {
				return (float)Math.PI/2;
			}
		}
		return 0;
	}
	
	public float getRotationY() {
		if(this instanceof TileEntityWindvane) {
			if(xCoord != coreX && coreY != 999) {
				return (float)Math.PI/2;
			}
			if(xCoord == coreX && zCoord != coreZ) {
				return 0;
			}
			if(zCoord == millZ) {
				return -(float)Math.PI/2;
			}
		}
		if(coreY != 999 && getAxelPosition() == 3) {
			if((zCoord - coreZ) > 0) {
				return -1;
			}
			else if((zCoord - coreZ) < 0) {
				return 0;
			}
			if((xCoord - coreX) > 0) {
				return 1;
			}
			else if((xCoord - coreX) < 0) {
				return 2;
			}
		}
		else if(coreY != 999 && Math.abs(zCoord - coreZ) == 0 && Math.abs(zCoord - millZ) == 0) {
			return -(float)Math.PI/2;
		}
		return 0;
	}
	
	public float getRotationZ() {
		return 0;
	}
	
	public int getAxelPosition() {
		return Math.abs(xCoord - coreX) + Math.abs(zCoord - coreZ);
	}
	
	public int getPower() {
		if(coreX == xCoord && coreY == yCoord && coreZ == zCoord) {
			return powerAmt;
		}
		else {
			TileEntityWindmill te = (TileEntityWindmill)this.worldObj.getTileEntity(coreX, coreY, coreZ);
			if(te!=null)
				return te.getPower();
		}
		return 0;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		writeToNBT(nbtTag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}

	public static float powerScale(TileEntity tileEntity, int pow) {
		TileEntityWindmill te = (TileEntityWindmill)tileEntity;
		IMechanicalPowerUser tg = (IMechanicalPowerUser)te.worldObj.getTileEntity(te.millX, te.millY, te.millZ);
		return tg.powerScale(pow-tg.minimumTorque());
	}

	public static String displayString(TileEntity tileEntity) {
		TileEntityWindmill te = (TileEntityWindmill)tileEntity;
		IMechanicalPowerUser tg = (IMechanicalPowerUser)te.worldObj.getTileEntity(te.millX, te.millY, te.millZ);
		return tg.getActionString();
	}
}
