#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <zlib.h>

#include <stddef.h>
#include <sys/types.h>
#include <dirent.h>
#include <unistd.h>

#include <sys/time.h>

int readFileList(char *basePath, char files[1024][100])
{
    DIR *dir;
    struct dirent *ptr;
    int i = 0;

    if ((dir = opendir(basePath)) == NULL)
    {
        perror("Open dir error...");
        exit(1);
    }

    while ((ptr = readdir(dir)) != NULL)
    {
        if (strcmp(ptr->d_name, ".") == 0 || strcmp(ptr->d_name, "..") == 0) ///current dir OR parrent dir
            continue;
        else if (ptr->d_type == 8) ///file
        {
            //printf("file/d_name:%s/%s\n", basePath, ptr->d_name);
            if (i<1024)
            {
                strcpy(files[i], ptr->d_name);
            }
            i++;
        }
        /*
        else if (ptr->d_type == 10) ///link file
            printf("link/d_name:%s/%s\n", basePath, ptr->d_name);
        else if (ptr->d_type == 4) ///dir
            printf("link/d_name:%s/%s\n", basePath, ptr->d_name);
        */
    }
    closedir(dir);
    return 1;
}

#define GZFile gzFile
// gcc -o mr.bin mr.c -lm -lz -std=c99 -Wextra
// yum install -y zlib zlib-devel
int decompress_one_gzfile(char *gzfile, char *outfile)
{
    GZFile zfile;
    FILE *fp;
    int num_read;
    char buffer[128] = {0};

    zfile = gzopen(gzfile, "rb");
    if (zfile == NULL)
    {
        printf("gzopen file error!\n");
        return -1;
    }

    fp = fopen(outfile, "wb");
    if (fp == NULL)
    {
        printf("open file error!\n");
        return -1;
    }

    while ((num_read = gzread(zfile, buffer, sizeof(buffer))) > 0)
    {
        fwrite(buffer, 1, num_read, fp);
        memset(buffer, 0, 128);
    }

    fclose(fp);
    gzclose(zfile);
    return 0;
}

#define isTMAXFlag(buffer) (buffer[21] == 'T' && buffer[22] == 'M' && buffer[23] == 'A' && buffer[24] == 'X')
#define isTMINFlag(buffer) (buffer[21] == 'T' && buffer[22] == 'M' && buffer[23] == 'I' && buffer[24] == 'N')

int getTemperature(char *line)
{
    char temp[5] = {0};
    char c;
    int i = 26;
    int j = 0;
    while((c = line[i++]) != '\0')
    {
        if(c != ',')
        {
            temp[j++] = c;
        }
        else
        {
            break;
        }
        
    }
    return atoi(temp);
}

// max:0, min:1, else:-1
int getTMAXAndTMIN(char *line, int *temperature)
{
    char temp[5] = {0};
    char c;
    int i = 26;
    int j = 0;
    int ret = -1;
    if(isTMAXFlag(line))
    {
        ret = 0;
    }
    if(isTMINFlag(line))
    {
        ret = 1;
    }

    while((c = line[i++]) != '\0')
    {
        if(c != ',')
        {
            temp[j++] = c;
        }
        else
        {
            break;
        }
        
    }
    if(line[i] == ',' && line[i+1] == ',')
    {
        *temperature = atoi(temp);
    }
    else
    {
        ret = -1;
    }

    return ret;
}

int main(int argc, char **argv)
{
    if (argc != 3)
    {
        printf("use: %s [basePath] [outPath] \n", argv[0]);
        return 0;
    }
    char *basePath = argv[1];
    char *outPath = argv[2];
    //char currentDir[120];
    //getcwd(currentDir, sizeof(currentDir));

    int globalMaxTemperature = 0;
    int globalMinTemperature = 0;
    struct timeval tpstart,tpend;
    float timeuse;

    gettimeofday(&tpstart,NULL);

    char files[1024][100];
    memset(files, 0, sizeof(files));
    readFileList(basePath, files);
    for(unsigned int i=0; i < 1024; i++)
    {
        if(strlen(files[i]) > 0)
        {
            char *name = files[i];
            int len = strlen(name);
            if(name[len-1] == 'z' && name[len-2] == 'g' && name[len-3] == '.')
            {
                printf("decompress_one_gzfile: %s...\n", files[i]);
                char csvfile[100];
                strcpy(csvfile, files[i]);
                int csvFileLen = strlen(csvfile);
                csvfile[csvFileLen-3] = '\0';

                char filePath[100];
                strcpy(filePath, basePath);
                strcat(filePath,"/");
                strcat(filePath,files[i]);
                char outFile[100];
                strcpy(outFile, outPath);
                strcat(outFile,"/");
                strcat(outFile,csvfile);
                int ret = decompress_one_gzfile(filePath, outFile);
                if (ret == 0)
                {
                    FILE *fp;
                    char buffer[128] = {0};
                    fp = fopen(outFile, "rb");
                    if (fp != NULL)
                    {
                        printf("read_one_gzfile: %s...\n", outFile);
                        while(fgets(buffer,sizeof(buffer),fp) != NULL)
                        {
                            /*
                            int temperature;
                            int getTemp = getTMAXAndTMIN(buffer, &temperature);
                            if(getTemp == 0)
                            {
                                if(globalMaxTemperature < temperature)
                                {
                                    globalMaxTemperature = temperature;
                                }
                            }
                            else if(getTemp == 1)
                            {
                                if(globalMinTemperature > temperature)
                                {
                                    globalMinTemperature = temperature;
                                }
                            }*/
                            
                            if(isTMAXFlag(buffer))
                            {
                                int temp = getTemperature(buffer);
                                if(globalMaxTemperature < temp)
                                {
                                    globalMaxTemperature = temp;
                                }
                            }
                            else if(isTMINFlag(buffer))
                            {
                                int temp = getTemperature(buffer);
                                if(globalMinTemperature > temp)
                                {
                                    globalMinTemperature = temp;
                                }
                            }
                            
                            memset(buffer, 0, sizeof(buffer));
                        }
                    }
                }
            }
        }
        else
        {
            break;
        }
    }

    printf("globalMaxTemperature= %d, globalMinTemperature= %d\n", globalMaxTemperature,globalMinTemperature);
    gettimeofday(&tpend,NULL); 
    timeuse=1000000*(tpend.tv_sec-tpstart.tv_sec)+ tpend.tv_usec-tpstart.tv_usec; 
    timeuse/=1000000; 
    printf("Used Time:%f s\n",timeuse); 

    return 0;
}