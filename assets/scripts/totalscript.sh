#!/system/bin/sh
# script for DATAFIX
echo " **  SCRIPT FOR DATAFIX ** "

prepare_runtime()
{
	echo " prepare_runtime"
	mount -o rw,remount -t yaffs2 /dev/block/mtdblock2 /system
		
	if [ $2 = copyscript ]; then
		rm /system/etc/init.d/30datafix*
		rm /system/etc/init.d/S30datafix*
		rm /data/data/.datafix_ng
		cd /data/data/by.zatta.datafix/files
		cat datafix_ng_busybox > /system/etc/init.d/$1datafix_ng_busybox
		chmod 777 /system/etc/init.d/$1datafix_ng_busybox
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
}

wipe_cache()
{

	# DRAWBACK FOR-DO USING  * IS TAKING EVERY WORD AS ENTRY. SO A NAME WITH
	# A WHITE SPACE IN ITS NAME IS NOT GETTING HANDLED PROPERLY

SAVEIFS=$IFS
IFS=$'\n'
		
	cache="/data/data/$1/cache"
	if [[ -L "$cache" || ! -d "$cache" || -L "/data/data" ]]; then
		echo "wiping yaffs" &&
		cache="/datadata/$1/cache"
	else
		echo "wiping data/data"			
	fi
	
	# check if there really is a cache, else it will wipe unexpected stuff
	if [[ -d "$cache" ]]; then
		cd "$cache" &&
		for item in *; do
			if [[  -d "$item" ]]; then
				echo "$item is directory"
				#rm -rf $item
			else
				echo "$item is file"
				#rm -f $item
			fi
		done
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
	# there is no folder for this app. Must be no datafix yet.
	if [ ! -d "$basePath" ]; then
		basePath="/datadata/$1"
	fi
	
	cd "$basePath" &&
	for firstLevel in *; do
		#if the entry is a symLink or not excists it must be on /datadata
		tempBasePath="$basePath"
		if [[ -L "$firstLevel" || !  -d "$firstLevel" ]]; then
			
			if [[ "$firstLevel" != "lib" && "$firstLevel" != "libs" && -L "$firstLevel" ]]; then
				rm -f $firstLevel
			fi
			tempBasePath="/datadata/$1"
			cd "$tempBasePath"
		else
			tempBasePath="$basePath"
		fi		
		
		
		echo "starting in $tempBasePath $firstLevel"
		# We now are in the correct basePath. SymLink is destroyed.
		# check if we deal with a directory (not being lib, skip those) or a file
		if 	[[ "$firstLevel" != "lib" && "$firstLevel" != "libs"  && -d "$firstLevel" ]]; then
			#Going in 
			cd $firstLevel &&
			for secondLevel in *; do
				echo "second $secondLevel"
				if [[  -d "$secondLevel" ]]; then
					rm -rf $secondLevel
				else
					echo "$secondLevel is file"
					rm -f $secondLevel
				fi			
				cd $tempBasePath
			done
			rm -rf $firstLevel
			cd $basePath
			
		elif [[ "$firstLevel" != "lib" && "$firstLevel" != "libs" ]]; then
			echo "$firstLevel is file"
			rm -f $firstLevel
			cd $basePath
		fi
	done
	
IFS=$SAVEIFS
}

for i
do
  case "$i" in
		
	prepare_runtime) prepare_runtime $2 $3 $4;;
	wipe_cache) wipe_cache $2;;
	wipe_data) wipe_data $2;;
	
  esac
done
