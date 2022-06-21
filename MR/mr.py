#!/usr/bin/python
# -*- coding: UTF-8 -*-

import gzip
import os
import datetime
import sys

def findAllFile(base):
    for root, ds, fs in os.walk(base):
        for f in fs:
            fullname = os.path.join(root, f)
            yield fullname

def un_gz(zfile, csvfile):
    g_file = gzip.GzipFile(zfile)
    open(csvfile, "wb+").write(g_file.read())
    g_file.close()

def getMaxAndMinTemperature(basePath, csvOutPath):
    max = 0
    min = 0
    
    for file in findAllFile(basePath):
        if file.endswith('.csv.gz'):
            csvfile = csvOutPath + '/' + os.path.basename(file).replace('.csv.gz', '.csv')
            un_gz(file, csvfile)
            
            print(file)
            
            f = open(csvfile)
            line = f.readline()
            while line:
                #print(line)
                splits = line.split(',')
                if splits[2] == 'TMAX' and max < int(splits[3]):
                    max = int(splits[3])
                if splits[2] == 'TMIN' and min > int(splits[3]):
                    min = int(splits[3])
                line = f.readline()
            f.close()
    return max,min
    
    
if __name__ == '__main__':
    #basePath = "D:\\DownLoad\\climateExample"; For Linux:/opt/data
    #csvOutPath = "C:\\Users\\haiye\\Desktop"
    
    if len(sys.argv) != 3:
        print('Usage:' + sys.argv[0] + ' [basePath] [csvOutPath]')
        exit(-1)
    basePath = sys.argv[1]
    csvOutPath = sys.argv[2]
    
    starttime = datetime.datetime.now()
    
    max,min = getMaxAndMinTemperature(basePath, csvOutPath)
    
    print(max)
    print(min)
    
    endtime = datetime.datetime.now()
    print('Eclipsed Time： {}s'.format((endtime - starttime).seconds))
    