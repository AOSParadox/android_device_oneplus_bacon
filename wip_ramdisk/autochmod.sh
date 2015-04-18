#!/system/bin/sh
DATE=`date +%F-%H`
CURTIME=`date +%F-%H-%M-%S`
ROOT_AUTOTRIGGER_PATH=/sdcard/oneplus_log
ROOT_TRIGGER_PATH=/sdcard/oneplus_log/trigger

config="$1"

function Preprocess(){
	mkdir -p $ROOT_AUTOTRIGGER_PATH
	mkdir -p  $ROOT_TRIGGER_PATH
}

function PreprocessLog(){
    sdcardFreeSize=`df /sdcard | grep /sdcard | /system/xbin/busybox awk '{print $4}'`
    #sdcard1FreeSize=`df /storage/sdcard1 | grep /sdcard1 | /system/xbin/busybox awk '{print $4}'`
    LOGDATE=`date +%F`
    LOGTIME=`date +%H%M%S`
    DIRTIME=`date +%F-%H%M%S`
#if [ ${sdcard1FreeSize} = "0.0K" ] || [ -z ${sdcard1FreeSize} ]; then
    ROOT_SDCARD_LOG_PATH=/sdcard/oneplus_log/${DIRTIME}_current
    mkdir -p  ${ROOT_SDCARD_LOG_PATH}
    isM=`echo ${sdcardFreeSize} | /system/xbin/busybox awk '{ print index($1,"M")}'`
    FreeSize=${sdcardFreeSize}
#else
#    ROOT_SDCARD_LOG_PATH=/storage/sdcard1/oppo_log/${LOGDATE}
#    mkdir -p  ${ROOT_SDCARD_LOG_PATH}
#    isM=`echo ${sdcard1FreeSize} | /system/xbin/busybox awk '{ print index($1,"M")}'`
#    FreeSize=${sdcard1FreeSize}
#fi
    ROOT_SDCARD_apps_LOG_PATH=${ROOT_SDCARD_LOG_PATH}/apps
    ROOT_SDCARD_kernel_LOG_PATH=${ROOT_SDCARD_LOG_PATH}/kernel
    ROOT_SDCARD_netlog_LOG_PATH=${ROOT_SDCARD_LOG_PATH}/netlog
    mkdir -p  ${ROOT_SDCARD_apps_LOG_PATH}
    mkdir -p  ${ROOT_SDCARD_kernel_LOG_PATH}
    mkdir -p  ${ROOT_SDCARD_netlog_LOG_PATH}
    mkdir -p  ${ROOT_SDCARD_LOG_PATH}/assertlog
    mkdir -p  ${ROOT_SDCARD_LOG_PATH}/anr
	mkdir -p  ${ROOT_SDCARD_LOG_PATH}/tombstones
if [ ${isM} = "0" ]; then
    androidSize=20480
    androidCount=`echo ${FreeSize} 30 50 ${androidSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    radioSize=20480
    radioCount=`echo ${FreeSize} 1 50 ${radioSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    eventSize=20480
    eventCount=`echo ${FreeSize} 1 50 ${eventSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    tcpdumpSize=100
    tcpdumpCount=`echo ${FreeSize} 10 50 ${tcpdumpSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
else
    androidSize=20480
    androidCount=`echo ${FreeSize} 30 50 ${androidSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    radioSize=10240
    radioCount=`echo ${FreeSize} 1 50 ${radioSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    eventSize=10240
    eventCount=`echo ${FreeSize} 1 50 ${eventSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    tcpdumpSize=50
    tcpdumpCount=`echo ${FreeSize} 10 50 ${tcpdumpSize} | /system/xbin/busybox awk '{printf("%d",$1*$2/$3/$4)}'`
fi
}

function GetLogDir(){
sdcardFreeSize=`df /sdcard | grep /sdcard | /system/xbin/busybox awk '{print $4}'`
    isM=`echo ${sdcardFreeSize} | /system/xbin/busybox awk '{ print index($1,"M")}'`
    FreeSize=${sdcardFreeSize}
    sleep 1
	CURRENT_DIR=`find /sdcard/oneplus_log/ -name *current`
    ROOT_SDCARD_apps_LOG_PATH=${CURRENT_DIR}/apps
    ROOT_SDCARD_kernel_LOG_PATH=${CURRENT_DIR}/kernel
    ROOT_SDCARD_netlog_LOG_PATH=${CURRENT_DIR}/netlog
if [ ${isM} = "0" ]; then
    androidSize=20480
    androidCount=`echo ${FreeSize} 30 50 ${androidSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    radioSize=20480
    radioCount=`echo ${FreeSize} 1 50 ${radioSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    eventSize=20480
    eventCount=`echo ${FreeSize} 1 50 ${eventSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024*1024/$3/$4)}'`
    tcpdumpSize=100
    tcpdumpCount=`echo ${FreeSize} 10 50 ${tcpdumpSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
else
    androidSize=20480
    androidCount=`echo ${FreeSize} 30 50 ${androidSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    radioSize=10240
    radioCount=`echo ${FreeSize} 1 50 ${radioSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    eventSize=10240
    eventCount=`echo ${FreeSize} 1 50 ${eventSize} | /system/xbin/busybox awk '{printf("%d",$1*$2*1024/$3/$4)}'`
    tcpdumpSize=50
    tcpdumpCount=`echo ${FreeSize} 10 50 ${tcpdumpSize} | /system/xbin/busybox awk '{printf("%d",$1*$2/$3/$4)}'`
fi
}

function Preprocess_other(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}
	GRAB_PATH=$ROOT_TRIGGER_PATH/${CURTIME}
}

function Preprocess_tra(){
	mkdir -p  $ROOT_AUTOTRIGGER_PATH/${CURTIME}
	TRN_PATH=$ROOT_AUTOTRIGGER_PATH/${CURTIME}
}

function Logcat(){
	system/bin/logcat -f ${ROOT_SDCARD_apps_LOG_PATH}/android.txt -r ${androidSize} -n ${androidCount} -v threadtime *:V
}
function Logcat_radio(){
	system/bin/logcat -b radio -f ${ROOT_SDCARD_apps_LOG_PATH}/radio.txt -r ${radioSize} -n ${radioCount} -v threadtime *:V
}
function Logcat_event(){
	system/bin/logcat -b events -f ${ROOT_SDCARD_apps_LOG_PATH}/events.txt  -r ${eventSize} -n ${eventCount} -v threadtime *:V
}
function Logcat_kernel2(){
	system/xbin/klogd -f ${ROOT_SDCARD_kernel_LOG_PATH}/kinfox.txt -n -x -l 7
}
function tcpdump_log(){
	system/xbin/tcpdump -i any -p -s 0 -W ${tcpdumpCount} -C ${tcpdumpSize} -w ${ROOT_SDCARD_netlog_LOG_PATH}/tcpdump.pcap -Z root
}

function Logcat_kernel(){
	#mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}
	#dmesg > $ROOT_TRIGGER_PATH/kinf11.txt;
	cat proc/kmsg > ${ROOT_SDCARD_kernel_LOG_PATH}/kinfo${LOGTIME}_x.txt;
}
function Dumpsys(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}_dumpsys
	dumpsys > $ROOT_TRIGGER_PATH/${CURTIME}_dumpsys/dumpsys.txt;
}
function Dumpstate(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}_dumpstate
	dumpstate > $ROOT_TRIGGER_PATH/${CURTIME}_dumpstate/dumpstate.txt
}
function Top(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}_top
	top -n 1 > $ROOT_TRIGGER_PATH/${CURTIME}_top/top.txt;
}
function Ps(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}_ps
	ps > $ROOT_TRIGGER_PATH/${CURTIME}_ps/ps.txt;
}

function Server(){
	mkdir -p  $ROOT_TRIGGER_PATH/${CURTIME}_servelist
	service list  > $ROOT_TRIGGER_PATH/${CURTIME}_servelist/serviceList.txt;
}

function BugReport(){
	Preprocess_other
	bugreport > $TRN_PATH/bugreport.txt
}

function CleanAll(){
        rm  -rf  /data/tombstones/tombstone*
        rm  -rf  /data/tombstones/dsps/*
        rm  -rf  /data/tombstones/lpass/*
        rm  -rf  /data/tombstones/modem/*
        rm  -rf  /data/tombstones/wcnss/*
	rm -rf  /sdcard/oneplus_log
}

function tranfer(){
    LOGDATE=`date +%F`
    mkdir /sdcard/oneplus_log/$LOGDATE/tombstones
    cp -rf  /data/tombstones /sdcard/oneplus_log/$LOGDATE/tombstones
       bugreport > /sdcard/oneplus_log/$LOGDATE/bugreport.txt
		
}

function asserttransfer(){
    chmod 777 /cache/assertlog/*
    mv /cache/assertlog/* $ROOT_AUTOTRIGGER_PATH/*current/assertlog/
    chmod 777 /data/anr/*
    mv /data/anr/* $ROOT_AUTOTRIGGER_PATH/*current/anr/
}

case "$config" in
	"ps")
		Preprocess
		Ps
		;;
	"top")
		Preprocess
		Top
		;;
	"server")
		Preprocess
		Server
		;;
	"dump")
		Preprocess
		Dumpsys
		;;
	"tranfer")
		Preprocess
		tranfer
		;;
	"asserttransfer")
		Preprocess
		asserttransfer
		;;
	"main")
                OLD_DIR=`find /sdcard/oneplus_log/ -name *current`
                DIR_TMP=`echo $OLD_DIR | sed "s/_current//"`
                mv $OLD_DIR $DIR_TMP
		PreprocessLog
		Logcat
		;;
	"radio")
		
		GetLogDir
		Logcat_radio
		;;
	"event")
		GetLogDir
		Logcat_event
		;;	
	"kernel")
		GetLogDir
		Logcat_kernel
		;;		
	"tcpdump")
		GetLogDir
		tcpdump_log
		;;
	"clean")		
		CleanAll
		;;
	"dumpstate")
		Preprocess	
		Dumpstate
		;;					
     *)
	tranfer
	 
      ;; 
esac





