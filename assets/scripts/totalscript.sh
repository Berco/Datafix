#!/system/xbin/sh
# script for DATAFIX

prepare_runtime()
{

	busybox mount -o rw,remount -t yaffs2 /dev/block/mtdblock2 /system
	
	if [[ $2 = files_and_script || $2 = full_update ]]; then
		busybox rm /system/etc/init.d/30datafix*
		busybox rm /system/etc/init.d/S30datafix*
		busybox cat /data/data/by.zatta.datafix/files/datafix_ng_busybox > /system/etc/init.d/$1datafix_ng_busybox
		busybox chown 0:2000 /system/etc/init.d/$1datafix_ng_busybox
		busybox chmod 755 /system/etc/init.d/$1datafix_ng_busybox
	fi
	
	busybox rm /system/addon.d/60-datafix.sh
	busybox cat /data/data/by.zatta.datafix/files/60-datafix.sh > /system/addon.d/60-datafix.sh
	busybox chown 0:2000 /system/addon.d/60-datafix.sh
	busybox chmod 755 /system/addon.d/60-datafix.sh
	
	if [ -f "/data/local/datafix" ]; then
		busybox rm /data/local/datafix
	fi
	
	if [ ! -d "/data/local/datafix" ]; then
		busybox mkdir -p "/data/local/datafix"
	fi
		
	busybox cat /data/data/by.zatta.datafix/files/move_cache.txt > /data/local/datafix/move_cache.txt
	busybox chmod 740 /data/local/datafix/move_cache.txt
	
	busybox cat /data/data/by.zatta.datafix/files/skip_apps.txt > /data/local/datafix/skip_apps.txt
	busybox chmod 740 /data/local/datafix/skip_apps.txt
	
	busybox echo "$4" > "/data/local/datafix/type"
	
	if [ $3 = reboot ]; then
		reboot
	fi
	
	if [ $3 = reboot_recovery ]; then
		cat extendedcommand > /cache/recovery/extendedcommand
		reboot recovery
	fi
}

wipe_cache()
{
	# DRAWBACK FOR-DO USING  * IS TAKING EVERY WORD AS ENTRY. SO A NAME WITH
	# A WHITE SPACE IN ITS NAME IS NOT GETTING HANDLED PROPERLY

SAVEIFS=$IFS
IFS=$'\n'
		
	cache="/data/data/$1/cache"
	# clean cache simplification : just remove all stuff in cache subdirectory no need to check where it is
	# ... but have to check if it's not a file
	# IF THEY'RE MASKED FILES IN CACHE THEY'RE NOT REMOVED
	if [ -e "$cache" -a ! -f "$cache" ]; then
		# Check if it not empty (something to remove ?)
		if [ "$(ls $cache)" ]; then
			echo "Wiping $1 cache";
			rm -rf -- "$cache"/*
		else
			echo "Nothing to clear"
		fi
	# if it's a file : emptying it
	elif [ -f "$cache" ]; then
		cat /dev/null > "$cache"
	fi
	
IFS=$SAVEIFS
}

wipe_data()
{
SAVEIFS=$IFS
IFS=$'\n'

	# DRAWBACK FOR-DO USING  * IS TAKING EVERY WORD AS ENTRY. SO A NAME WITH
	# A WHITE SPACE IN ITS NAME IS NOT GETTING HANDLED PROPERLY
	
	
	basePath="/data/data/$1"

	cd "$basePath" &&
	for d in *; do
		# if $dir is not a file (so a dir or a symlink)
		if [[ "$d" != "lib" && "$d" != "libs" && ! -f "$d" ]]; then
		# Check if it not empty (something to remove ?)
			if [ "$(ls $d)" ]; then
				rm -rf -- "$d"/*
			fi
		# if it's a file : emptying it
		elif [[ "$d" != "lib" && "$d" != "libs" &&  -f "$d" ]]; then
			cat /dev/null > "$d"
		fi
	done
	
IFS=$SAVEIFS
}

check_sizes()
{
if [[ "$1" != "no_check" ]]; then

	#lines for testing purposes
	#if [ ! -e "/data/data/by.zatta.datafix/thislink" ]; then
	#	busybox ln -s "/sdcard/Cyandelta" "/data/data/by.zatta.datafix/thislink"
	#fi
	
	size_total=$(busybox du -sLc /data/data|busybox tail -1|busybox cut -f1)
	size_lib=$(busybox du -sLc /data/data/*/li*|busybox tail -1|busybox cut -f1)
	size_avail=$(busybox df -k | grep "/datadata" | busybox awk -F " " '{ print $2  }')
	#making it possible for Zatta to debug on an device not containing /datadata
	if [ -z $size_avail ]; then
		size_avail=400000
	fi
	size_cache=0
	size_skip=0
	exclude=0
		
	if [[ "$1" = "advanced_check" ]]; then
		for app in $(busybox cat /data/data/by.zatta.datafix/files/move_cache.txt) ; do
			if [[ ! `busybox grep "$app" "/data/data/by.zatta.datafix/files/skip_apps.txt"` ]]; then
				app_cache=$(busybox du -sLc /data/data/$app/cache|busybox tail -1|busybox cut -f1)
				size_cache=$(($size_cache + $app_cache))
			fi
		done
		
		for app in $(busybox cat /data/data/by.zatta.datafix/files/skip_apps.txt) ; do
			app_skip=$(busybox du -sLc /data/data/$app/*|busybox tail -1|busybox cut -f1)
			if [ -d /data/data/$app/li* ]; then
				app_lib=$(busybox du -sLc /data/data/$app/li*|busybox tail -1|busybox cut -f1)
				app_skip=$(($app_skip - $app_lib))
			fi
			size_skip=$(($size_skip + $app_skip))
		done
		
		for base in data/data datadata ; do
		for mylink in $(busybox find /$base -type l) ; do
			link=$(busybox ls -l $mylink | busybox awk -F " " '{ print $11  }' | busybox awk -F "/" '{ print $2 }' )
			if [[ ! "$link" = "datadata" ]]; then
				exclude_this=$(busybox du -sLc $mylink | busybox tail -1 | busybox cut -f1)
				exclude=$(( $exclude + $exclude_this ))
			fi
		done	
	done
	fi
	
	dif=$(($size_total - $size_lib - $size_cache - $size_skip - $exclude))
	minimum=$(($size_avail - 5000))
	
	echo "ZEOL TOTALS ZEOL avail:$size_avail ZEOL ZEOL total:$size_total ZEOL lib:$size_lib ZEOL cache:$size_cache ZEOL skip:$size_skip ZEOL dif:$dif ZEOL exclude:$exclude ZEOL"
	
	if [ $dif -gt $minimum ]; then
		shortage=$(($dif - $minimum))
		echo "$shortage"
	else
		result=$(($dif - $minimum))
		echo "result will be $result ZEOL"
		echo "okay"
	fi
else
	echo "UNCHECKED"
fi
}

for i
do
  case "$i" in
	prepare_runtime) prepare_runtime $2 $3 $4 $5;;
	wipe_cache) wipe_cache $2;;
	wipe_data) wipe_data $2;;
	check_sizes) check_sizes $2;;	
  esac
done
