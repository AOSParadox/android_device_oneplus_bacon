#!/system/bin/sh

###################################################################################################
if [ -f system/bin/su -o -f /system/xbin/su -o -f /system/sbin/su -o -f /sbin/su -o -f /vendor/bin/su ]; then    	
		setprop persist.sys.root.state 1		
fi
######################################################################################################
