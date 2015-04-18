#!/system/bin/sh

#ifdef VENDOR_EDIT
#Peirs@Swdp.Android.FrameworkUi, 2014/07/30, add provide a function to indentify the usage of the phone.
#recommend set in the stage of "on post-fs"
chmod 0660 /dev/block/mmcblk0p27
chown root.system /dev/block/mmcblk0p27
#endif
