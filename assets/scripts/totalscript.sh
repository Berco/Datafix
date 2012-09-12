#!/system/bin/sh
# script for DATAFIX

prepare_runtime()
{
	mount -o rw,remount -t yaffs2 /dev/block/mtdblock2 /system
	
	if [[ $2 = files_and_script || $2 = full_update ]]; then
		rm /system/etc/init.d/30datafix*
		rm /system/etc/init.d/S30datafix*
		cd /data/data/by.zatta.datafix/files
		cat datafix_ng_busybox > /system/etc/init.d/$1datafix_ng_busybox
		chown 0:2000 /system/etc/init.d/$1datafix_ng_busybox
		chmod 755 /system/etc/init.d/$1datafix_ng_busybox
	fi
	
	if [ -f "/data/local/datafix" ]; then
		rm /data/local/datafix
	fi
	
	if [ ! -d "/data/local/datafix" ]; then
		mkdir -p "/data/local/datafix"
	fi
	
	cd /data/data/by.zatta.datafix/files
	
	cat move_cache.txt > /data/local/datafix/move_cache.txt
	chmod 740 /data/local/datafix/move_cache.txt
	
	cat skip_apps.txt > /data/local/datafix/skip_apps.txt
	chmod 740 /data/local/datafix/skip_apps.txt
	
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
	size_total=$(busybox du -sLc /data/data|busybox tail -1|busybox cut -f1)
	size_lib=$(busybox du -sLc /data/data/*/li*|busybox tail -1|busybox cut -f1)
	size_avail=$(busybox df -k /datadata | busybox tail -1 | busybox awk -F " " '{ print $2  }')

	size_cache=0
	for app in $(busybox cat /data/data/by.zatta.datafix/files/move_cache.txt) ; do
		if [[ ! `busybox grep "$app" "/data/data/by.zatta.datafix/files/skip_apps.txt"` ]]; then
			app_cache=$(busybox du -sLc /data/data/$app/cache|busybox tail -1|busybox cut -f1)
			size_cache=$(($size_cache + $app_cache))
		fi
	done
	
	size_skip=0
	for app in $(busybox cat /data/data/by.zatta.datafix/files/skip_apps.txt) ; do
		size_skip=$(busybox du -sLc /data/data/$app/*|busybox tail -1|busybox cut -f1)
		if [[ -d "/data/data/$app/li*" ]]; then
			app_lib=$(busybox du -sLc /data/data/$app/li*|busybox tail -1|busybox cut -f1)
			size_skip=$(($size_skip - $app_lib))
		fi
	done
	
	dif=$(($size_total - $size_lib - $size_cache - $size_skip))
	minimum=$(($size_avail - 5000))
	
	echo "avail:$size_avail dif:$dif total:$size_total lib:$size_lib cache:$size_cache skip:$size_skip"
	
	if [ $dif -gt $minimum ]; then
		shortage=$(($dif - $minimum))
		echo "$shortage"
	else
		echo "okay"
		#shortage=$(($dif - $minimum))
		#echo "$shortage"
	fi
}

for i
do
  case "$i" in
		
	prepare_runtime) prepare_runtime $2 $3 $4;;
	wipe_cache) wipe_cache $2;;
	wipe_data) wipe_data $2;;
	check_sizes) check_sizes;;	
  esac
done
