//
// Created by iiesm on 2023/8/24.
//


#define BYTE_SWAP

#ifdef CORE_TIME
#  undef BYTE_SWAP
#endif

#include "loki.h"
#include "stdio.h"
#include <time.h>

static char *alg_name[] = { "loki", "loki.c", "loki97" };

char **cipher_name()
{
    return alg_name;
};

#define MR_ECB 0
#define MR_CBC 1

typedef struct {
    int mode;
    u4byte llkey[96];
    u1byte siv[16];
} lokicc;

#define MR_TOBYTE(x) ((u1byte)((x)&0xFF))
#define NB 4
typedef  u4byte MR_WORD;
typedef  u1byte MR_BYTE;

#define NUM 1048576

static u4byte pack(const u1byte *b)
{ /* pack bytes into a 32-bit Word */
    return ((u4byte)b[3]<<24)|((u4byte)b[2]<<16)|((u4byte)b[1]<<8)|(u4byte)b[0];
}

static void unpack(u4byte a,u1byte *b)
{ /* unpack bytes from a word */
    b[0]=MR_TOBYTE(a);
    b[1]=MR_TOBYTE(a>>8);
    b[2]=MR_TOBYTE(a>>16);
    b[3]=MR_TOBYTE(a>>24);
}



#define S1_SIZE     13
#define S1_LEN      (1 << S1_SIZE)  //即为0x00002000
#define S1_MASK     (S1_LEN - 1)    //13位，即为0x00001fff
#define S1_HMASK    (S1_MASK & ~0xff)
#define S1_POLY     0x2911//不可约多项式,即为x^13+x^11+x^8+x^4+1
//可以修改使用其他不可约多项式

#define S2_SIZE     11
#define S2_LEN      (1 << S2_SIZE)//即为0x00000800
#define S2_MASK     (S2_LEN - 1)   //即为0x000007ff
#define S2_HMASK    (S2_MASK & ~0xff)
#define S2_POLY     0x0aa7  //不可约多项式,即为x^11+x^9+x^7+x^5++x^2+x+1
//可以修改使用其他不可约多项式

u4byte  delta[2] = { 0x7f4a7c15, 0x9e3779b9 };//存储Delta

u1byte  sb1[S1_LEN]=
        {0xe4,0x4b,0xbc,0x5a,0x9f,0xb1,0xf9,0x55,0x6a,0x38,0x09,0x8e,0xad,0xa9,0x60,0x1e,
         0xc3,0xd7,0x35,0x4d,0x16,0x86,0xaa,0xd8,0x0a,0x10,0x07,0xc1,0x7e,0x2e,0xa1,0xba,
         0x11,0xd6,0xe6,0xeb,0x8a,0xe9,0x9e,0x7a,0xff,0x0f,0x31,0x7b,0xd0,0x1a,0xee,0xcb,
         0x4c,0x52,0x79,0x01,0x14,0x1f,0x1b,0x65,0xa7,0xc7,0x33,0xfe,0x66,0x30,0xab,0x78,
         0x24,0x59,0x3c,0x36,0xf3,0x0b,0x45,0xa6,0x43,0x95,0x27,0x6e,0xc4,0x0e,0x81,0xd9,
         0x26,0x96,0xaf,0x71,0x37,0xbf,0x18,0x77,0x08,0x82,0x06,0x58,0x3d,0x28,0x44,0x4f,
         0x3e,0x8b,0x7d,0xf8,0x84,0x3a,0x9d,0x3f,0x22,0xb9,0x64,0xb4,0x98,0x32,0x0d,0xc9,
         0xcd,0xca,0x46,0xdf,0xa4,0x2c,0x29,0x9c,0x19,0xa0,0x5f,0x6c,0x8c,0x85,0xb6,0x2f,
         0x41,0x74,0x9a,0x42,0xfb,0x00,0xf0,0x53,0xd2,0x7c,0x12,0x2a,0x87,0xe0,0xa3,0x91,
         0xc6,0x21,0x34,0x8d,0x40,0x67,0x47,0xb5,0xb7,0x89,0x93,0x68,0x97,0x5c,0xf6,0x3b,
         0x05,0xa5,0x03,0xc0,0x6b,0xed,0xdb,0xd5,0x1c,0xcf,0x51,0x2d,0x99,0xc8,0xb8,0xdd,
         0xd1,0xcc,0xbb,0x13,0xfd,0x15,0x88,0xa8,0xf2,0x6d,0xae,0xa2,0x4a,0xea,0x5e,0xe7,
         0xe8,0x04,0x80,0x76,0x75,0x2b,0xfa,0xd4,0xe5,0xb2,0x5d,0x62,0x8f,0xec,0x48,0x70,
         0x69,0x0c,0xb0,0xef,0x4e,0xe2,0x02,0xda,0xbd,0x9b,0x17,0x39,0xbe,0x7f,0x72,0x54,
         0x6f,0xf1,0xc5,0x57,0x92,0xde,0xb3,0xfc,0x90,0x56,0x50,0xce,0xac,0xf7,0xe1,0x94,
         0x83,0xd3,0x63,0xf4,0x5b,0xe3,0x73,0xf5,0x1d,0x49,0xdc,0x23,0x25,0xc2,0x61,0x20,};
//S1

u1byte  sb2[S2_LEN]=
        {0x2c,0xe9,0x37,0x50,0x7c,0x2e,0x84,0xd9,0xaa,0xd0,0x80,0x39,0x36,0xf8,0x1e,0xdd,
         0x76,0x99,0x98,0xcd,0x02,0x3f,0xea,0x1d,0x01,0x54,0xaf,0x24,0x92,0x26,0x07,0x7b,
         0xeb,0x7e,0xbb,0xdf,0xc6,0xfe,0xe0,0x29,0xf7,0xe8,0x4e,0xd7,0xb5,0xa2,0x35,0x04,
         0xe2,0x8f,0x70,0xb3,0xd5,0xbd,0x03,0x1b,0x1a,0x8a,0xa9,0x48,0x18,0x08,0x16,0x8c,
         0x56,0x5f,0x0a,0x73,0xfa,0xf6,0x88,0x9e,0x46,0x0d,0x4b,0xba,0x90,0x47,0xbf,0x3e,
         0xe4,0x94,0xbc,0x8e,0x7f,0x4a,0x14,0xca,0xc0,0xd4,0x81,0xd3,0xf2,0xdb,0xde,0x22,
         0x9f,0x63,0xdc,0xbe,0xc4,0x19,0xd1,0x91,0xe6,0x61,0x43,0x23,0x69,0xf9,0x55,0x21,
         0xb2,0xb4,0xc7,0xda,0x4c,0x9a,0x82,0x9b,0xa8,0x96,0xb1,0x27,0x60,0x4f,0x45,0x75,
         0x8b,0xb9,0xd6,0xf5,0x53,0x8d,0xe1,0xae,0x58,0x66,0x6c,0xb8,0x28,0x0c,0xe7,0x0b,
         0xf0,0x5b,0xcb,0x7a,0xc1,0x42,0x59,0xec,0x85,0x31,0x97,0xd8,0x62,0x6d,0xfd,0x0e,
         0xa1,0x06,0x00,0x72,0x7d,0x51,0x38,0x5c,0xce,0x3b,0x2b,0x95,0x89,0x93,0xb6,0x41,
         0x40,0x5d,0x09,0x86,0x12,0x17,0x33,0x78,0x52,0xa5,0xc2,0x25,0x4d,0xa3,0x2f,0x74,
         0xad,0x67,0x64,0xa6,0x6f,0x15,0x87,0xfc,0xe3,0xed,0x05,0xcf,0xd2,0x5a,0x10,0x34,
         0xb7,0x32,0xc9,0x20,0xab,0x6a,0x77,0xc5,0x1f,0x2d,0xcc,0x9d,0x44,0x3c,0x3d,0xfb,
         0x68,0x1c,0xf4,0x57,0xf3,0x2a,0x49,0xef,0x79,0xa4,0xa0,0x83,0xa7,0xe5,0x3a,0xff,
         0xee,0x71,0x9c,0x30,0xf1,0x13,0xc3,0xb0,0x11,0x0f,0xc8,0x65,0x6b,0x5e,0xac,0x6e,};
//S2
u4byte  prm[256][2]={0x0,0x0,0x80,0x0,0x8000,0x0,0x8080,0x0,0x800000,0x0,0x800080,0x0,0x808000,0x0,0x808080,0x0,0x80000000,0x0,0x80000080,0x0,0x80008000,0x0,0x80008080,0x0,0x80800000,0x0,0x80800080,0x0,0x80808000,0x0,0x80808080,0x0,0x0,0x80,0x80,0x80,0x8000,0x80,0x8080,0x80,0x800000,0x80,0x800080,0x80,0x808000,0x80,0x808080,0x80,0x80000000,0x80,0x80000080,0x80,0x80008000,0x80,0x80008080,0x80,0x80800000,0x80,0x80800080,0x80,0x80808000,0x80,0x80808080,0x80,0x0,0x8000,0x80,0x8000,0x8000,0x8000,0x8080,0x8000,0x800000,0x8000,0x800080,0x8000,0x808000,0x8000,0x808080,0x8000,0x80000000,0x8000,0x80000080,0x8000,0x80008000,0x8000,0x80008080,0x8000,0x80800000,0x8000,0x80800080,0x8000,0x80808000,0x8000,0x80808080,0x8000,0x0,0x8080,0x80,0x8080,0x8000,0x8080,0x8080,0x8080,0x800000,0x8080,0x800080,0x8080,0x808000,0x8080,0x808080,0x8080,0x80000000,0x8080,0x80000080,0x8080,0x80008000,0x8080,0x80008080,0x8080,0x80800000,0x8080,0x80800080,0x8080,0x80808000,0x8080,0x80808080,0x8080,0x0,0x800000,0x80,0x800000,0x8000,0x800000,0x8080,0x800000,0x800000,0x800000,0x800080,0x800000,0x808000,0x800000,0x808080,0x800000,0x80000000,0x800000,0x80000080,0x800000,0x80008000,0x800000,0x80008080,0x800000,0x80800000,0x800000,0x80800080,0x800000,0x80808000,0x800000,0x80808080,0x800000,0x0,0x800080,0x80,0x800080,0x8000,0x800080,0x8080,0x800080,0x800000,0x800080,0x800080,0x800080,0x808000,0x800080,0x808080,0x800080,0x80000000,0x800080,0x80000080,0x800080,0x80008000,0x800080,0x80008080,0x800080,0x80800000,0x800080,0x80800080,0x800080,0x80808000,0x800080,0x80808080,0x800080,0x0,0x808000,0x80,0x808000,0x8000,0x808000,0x8080,0x808000,0x800000,0x808000,0x800080,0x808000,0x808000,0x808000,0x808080,0x808000,0x80000000,0x808000,0x80000080,0x808000,0x80008000,0x808000,0x80008080,0x808000,0x80800000,0x808000,0x80800080,0x808000,0x80808000,0x808000,0x80808080,0x808000,0x0,0x808080,0x80,0x808080,0x8000,0x808080,0x8080,0x808080,0x800000,0x808080,0x800080,0x808080,0x808000,0x808080,0x808080,0x808080,0x80000000,0x808080,0x80000080,0x808080,0x80008000,0x808080,0x80008080,0x808080,0x80800000,0x808080,0x80800080,0x808080,0x80808000,0x808080,0x80808080,0x808080,0x0,0x80000000,0x80,0x80000000,0x8000,0x80000000,0x8080,0x80000000,0x800000,0x80000000,0x800080,0x80000000,0x808000,0x80000000,0x808080,0x80000000,0x80000000,0x80000000,0x80000080,0x80000000,0x80008000,0x80000000,0x80008080,0x80000000,0x80800000,0x80000000,0x80800080,0x80000000,0x80808000,0x80000000,0x80808080,0x80000000,0x0,0x80000080,0x80,0x80000080,0x8000,0x80000080,0x8080,0x80000080,0x800000,0x80000080,0x800080,0x80000080,0x808000,0x80000080,0x808080,0x80000080,0x80000000,0x80000080,0x80000080,0x80000080,0x80008000,0x80000080,0x80008080,0x80000080,0x80800000,0x80000080,0x80800080,0x80000080,0x80808000,0x80000080,0x80808080,0x80000080,0x0,0x80008000,0x80,0x80008000,0x8000,0x80008000,0x8080,0x80008000,0x800000,0x80008000,0x800080,0x80008000,0x808000,0x80008000,0x808080,0x80008000,0x80000000,0x80008000,0x80000080,0x80008000,0x80008000,0x80008000,0x80008080,0x80008000,0x80800000,0x80008000,0x80800080,0x80008000,0x80808000,0x80008000,0x80808080,0x80008000,0x0,0x80008080,0x80,0x80008080,0x8000,0x80008080,0x8080,0x80008080,0x800000,0x80008080,0x800080,0x80008080,0x808000,0x80008080,0x808080,0x80008080,0x80000000,0x80008080,0x80000080,0x80008080,0x80008000,0x80008080,0x80008080,0x80008080,0x80800000,0x80008080,0x80800080,0x80008080,0x80808000,0x80008080,0x80808080,0x80008080,0x0,0x80800000,0x80,0x80800000,0x8000,0x80800000,0x8080,0x80800000,0x800000,0x80800000,0x800080,0x80800000,0x808000,0x80800000,0x808080,0x80800000,0x80000000,0x80800000,0x80000080,0x80800000,0x80008000,0x80800000,0x80008080,0x80800000,0x80800000,0x80800000,0x80800080,0x80800000,0x80808000,0x80800000,0x80808080,0x80800000,0x0,0x80800080,0x80,0x80800080,0x8000,0x80800080,0x8080,0x80800080,0x800000,0x80800080,0x800080,0x80800080,0x808000,0x80800080,0x808080,0x80800080,0x80000000,0x80800080,0x80000080,0x80800080,0x80008000,0x80800080,0x80008080,0x80800080,0x80800000,0x80800080,0x80800080,0x80800080,0x80808000,0x80800080,0x80808080,0x80800080,0x0,0x80808000,0x80,0x80808000,0x8000,0x80808000,0x8080,0x80808000,0x800000,0x80808000,0x800080,0x80808000,0x808000,0x80808000,0x808080,0x80808000,0x80000000,0x80808000,0x80000080,0x80808000,0x80008000,0x80808000,0x80008080,0x80808000,0x80800000,0x80808000,0x80800080,0x80808000,0x80808000,0x80808000,0x80808080,0x80808000,0x0,0x80808080,0x80,0x80808080,0x8000,0x80808080,0x8080,0x80808080,0x800000,0x80808080,0x800080,0x80808080,0x808000,0x80808080,0x808080,0x80808080,0x80000000,0x80808080,0x80000080,0x80808080,0x80008000,0x80808080,0x80008080,0x80808080,0x80800000,0x80808080,0x80800080,0x80808080,0x80808000,0x80808080,0x80808080,0x80808080};
u4byte  init_done = 0;//标记初始化是否完成

u4byte  l_key[96];

#define add_eq(x,y)     (x)[1] += (y)[1] + (((x)[0] += (y)[0]) < (y)[0] ? 1 : 0)   //带进位的加法
#define sub_eq(x,y)     xs = (x)[0]; (x)[1] -= (y)[1] + (((x)[0] -= (y)[0]) > xs ? 1 : 0)



void init_tables(void)
{   u4byte  i, j, v;

    //初始化P盒

    for(i = 0; i < 256; ++i)
    {
        prm[i][0] = ((i &  1) << 7) | ((i &  2) << 14) | ((i &  4) << 21) | ((i &   8) << 28);
        prm[i][1] = ((i & 16) << 3) | ((i & 32) << 10) | ((i & 64) << 17) | ((i & 128) << 24);
    }
};

/*f函数*/
void f_fun(u4byte res[2], const u4byte in[2], const u4byte key[2])
{   u4byte  i, tt[2], pp[2];

    tt[0] = in[0]^key[0];
    tt[1] = in[1]^key[1];//修改密钥参与方式

    i = sb1[((tt[1] >> 24) | (tt[0] << 8)) & S1_MASK];
    pp[0]  = prm[i][0] >> 7; pp[1]  = prm[i][1] >> 7;
    i = sb2[(tt[1] >> 16) & S2_MASK];
    pp[0] |= prm[i][0] >> 6; pp[1] |= prm[i][1] >> 6;
    i = sb1[(tt[1] >>  8) & S1_MASK];
    pp[0] |= prm[i][0] >> 5; pp[1] |= prm[i][1] >> 5;
    i = sb2[tt[1] & S2_MASK];
    pp[0] |= prm[i][0] >> 4; pp[1] |= prm[i][1] >> 4;
    i = sb2[((tt[0] >> 24) | (tt[1] << 8)) & S2_MASK];
    pp[0] |= prm[i][0] >> 3; pp[1] |= prm[i][1] >> 3;
    i = sb1[(tt[0] >> 16) & S1_MASK];
    pp[0] |= prm[i][0] >> 2; pp[1] |= prm[i][1] >> 2;
    i = sb2[(tt[0] >>  8) & S2_MASK];
    pp[0] |= prm[i][0] >> 1; pp[1] |= prm[i][1] >> 1;
    i = sb1[tt[0] & S1_MASK];
    pp[0] |= prm[i][0];      pp[1] |= prm[i][1];

    //自行设计E盒，满足Sb层输入要求
    res[0] ^=  sb1[pp[0]& S1_HMASK]
               | (sb1[pp[0]>>8 & S1_HMASK] << 8)
               | (sb2[pp[0]>>16 & S2_HMASK] << 16)
               | (sb2[(pp[0]>>24 | pp[1] << 8) & S2_HMASK] << 24);
    res[1] ^=  sb1[pp[1] & S1_HMASK]
               | (sb1[pp[0]>>8  & S1_HMASK] << 8)
               | (sb2[pp[0]>>16 & S2_HMASK] << 16)
               | (sb2[(pp[0]>>24 | pp[0] << 8) & S2_HMASK] << 24);
};
/*密钥生成算法*/
u4byte *set_key(const u4byte in_key[], const u4byte key_len)
{   u4byte  i, k1[2], k2[2], k3[2], k4[2], del[2], tt[2], sk[2];

    if(!init_done)
    {
        init_tables(); init_done = 1;
    }
    /*根据密钥生成算法,将K4=Ka,K3=Kb*/
    k4[0] = in_key[1]; k4[1] = in_key[0];
    k3[0] = in_key[3]; k3[1] = in_key[2];

    /*对128比特,192比特,256比特分别执行不同的操作*/
    switch ((key_len + 63) / 64)
    {
        case 2://128比特密钥
            k2[0] = 0; k2[1] = 0; f_fun(k2, k3, k4);
            k1[0] = 0; k1[1] = 0; f_fun(k1, k4, k3);
            break;
        case 3://192比特密钥
            k2[0] = in_key[5]; k2[1] = in_key[4];
            k1[0] = 0; k1[1] = 0; f_fun(k1, k4, k3);
            break;
        case 4: //256比特密钥
            k2[0] = in_key[5]; k2[1] = in_key[4];
            k1[0] = in_key[7]; k1[1] = in_key[6];
    }

    del[0] = delta[0]; del[1] = delta[1];

    for(i = 0; i < 48; ++i)//48轮
    {
        tt[0] = k1[0]; tt[1] = k1[1];
        add_eq(tt, k3);
        add_eq(tt, del);//K1+K3+del
        add_eq(del, delta);//Delta*i
        sk[0] = k4[0]; sk[1] = k4[1];
        k4[0] = k3[0]; k4[1] = k3[1];
        k3[0] = k2[0]; k3[1] = k2[1];
        k2[0] = k1[0]; k2[1] = k1[1];
        k1[0] = sk[0]; k1[1] = sk[1];
        f_fun(k1, tt, k3);
        l_key[2*i] = k1[0]; l_key[2*i+1] = k1[1];
    }

    return l_key;
};

#define r_fun(l,r,k)        \
    add_eq((l),(k));        \
    f_fun((r),(l),(k) + 2); \
    add_eq((l), (k) + 4)

void encrypt(const u4byte in_blk[4], u4byte out_blk[4])
{   u4byte  blk[4];

    blk[3] = in_blk[0]; blk[2] = in_blk[1];
    blk[1] = in_blk[2]; blk[0] = in_blk[3];

    r_fun(blk, blk + 2, l_key +  0);
    r_fun(blk + 2, blk, l_key +  6);
    r_fun(blk, blk + 2, l_key + 12);
    r_fun(blk + 2, blk, l_key + 18);
    r_fun(blk, blk + 2, l_key + 24);
    r_fun(blk + 2, blk, l_key + 30);
    r_fun(blk, blk + 2, l_key + 36);
    r_fun(blk + 2, blk, l_key + 42);
    r_fun(blk, blk + 2, l_key + 48);
    r_fun(blk + 2, blk, l_key + 54);
    r_fun(blk, blk + 2, l_key + 60);
    r_fun(blk + 2, blk, l_key + 66);
    r_fun(blk, blk + 2, l_key + 72);
    r_fun(blk + 2, blk, l_key + 78);
    r_fun(blk, blk + 2, l_key + 84);
    r_fun(blk + 2, blk, l_key + 90);

    out_blk[3] = blk[2]; out_blk[2] = blk[3];
    out_blk[1] = blk[0]; out_blk[0] = blk[1];
};

#define ir_fun(l,r,k)       \
    sub_eq((l),(k) + 4);    \
    f_fun((r),(l),(k) + 2); \
    sub_eq((l),(k))

void decrypt(const u4byte in_blk[4], u4byte out_blk[4])
{   u4byte  blk[4], xs;

    blk[3] = in_blk[0]; blk[2] = in_blk[1];
    blk[1] = in_blk[2]; blk[0] = in_blk[3];

    ir_fun(blk, blk + 2, l_key + 90);
    ir_fun(blk + 2, blk, l_key + 84);
    ir_fun(blk, blk + 2, l_key + 78);
    ir_fun(blk + 2, blk, l_key + 72);
    ir_fun(blk, blk + 2, l_key + 66);
    ir_fun(blk + 2, blk, l_key + 60);
    ir_fun(blk, blk + 2, l_key + 54);
    ir_fun(blk + 2, blk, l_key + 48);
    ir_fun(blk, blk + 2, l_key + 42);
    ir_fun(blk + 2, blk, l_key + 36);
    ir_fun(blk, blk + 2, l_key + 30);
    ir_fun(blk + 2, blk, l_key + 24);
    ir_fun(blk, blk + 2, l_key + 18);
    ir_fun(blk + 2, blk, l_key + 12);
    ir_fun(blk, blk + 2, l_key +  6);
    ir_fun(blk + 2, blk, l_key);

    out_blk[3] = blk[2]; out_blk[2] = blk[3];
    out_blk[1] = blk[0]; out_blk[0] = blk[1];
};


void lokicc_ecb_encrypt(lokicc *a,MR_BYTE *buff)
{
    int i,j,k;
    MR_WORD p[4],q[4],*t;


    for (i=j=0;i<NB;i++,j+=4)
    {
        p[i]=pack((MR_BYTE *)&buff[j]);
    }

    encrypt(p,q);


    for (i=j=0;i<NB;i++,j+=4)
    {
        unpack(q[i],(MR_BYTE *)&buff[j]);
    }
}


void lokicc_ecb_decrypt(lokicc *a,MR_BYTE *buff){
    int i,j,k;
    MR_WORD p[4],q[4],*t;


    for (i=j=0;i<NB;i++,j+=4)
    {
        p[i]=pack((MR_BYTE *)&buff[j]);
    }

    decrypt(p,q);

    for (i=j=0;i<NB;i++,j+=4)
    {
        unpack(q[i],(MR_BYTE *)&buff[j]);
    }

}


void lokicc_init(lokicc* a,int mode,char *key,char *iv){
    u4byte u4key[8];
    int i,j;

    for (i=j=0;i<8;i++,j+=4)
    {
        u4key[i]=pack((MR_BYTE *)&key[j]);
    }

    a->mode=mode;

    set_key(u4key,256);

    for(int i=0;i<16;i++)
        a->siv[i]=iv[i];
}

int lokicc_encrypt(lokicc* a,char *buff)
{
    int j,bytes;
    char st[16];

    switch (a->mode)
    {
        case MR_ECB:
            lokicc_ecb_encrypt(a,(MR_BYTE *)buff);
            return 0;
        case MR_CBC:
            for (j=0;j<4*NB;j++) buff[j]^=a->siv[j];
            lokicc_ecb_encrypt(a,(MR_BYTE *)buff);
            for (j=0;j<4*NB;j++) a->siv[j]=buff[j];
            return 0;

        default:
            return -1;
    }
}


int lokicc_decrypt(lokicc *a,char *buff)
{
    int j,bytes;
    char st[16];

    /* Supported modes of operation */
    switch (a->mode)
    {
        case MR_ECB:
            lokicc_ecb_decrypt(a,(MR_BYTE *)buff);
            return 0;
        case MR_CBC:
            for (j=0;j<4*NB;j++)
            {
                st[j]=a->siv[j];
                a->siv[j]=buff[j];
            }
            lokicc_ecb_decrypt(a,(MR_BYTE *)buff);
            for (j=0;j<4*NB;j++)
            {
                buff[j]^=st[j];
                st[j]=0;
            }
            return 0;
    }
}

//文件加密示例函数
int lokicc_encry_file(char infile[256],char outfile[256],u1byte key[32],u1byte iv[16]){
    lokicc a;
    FILE *fp1,*fp2;
    u4byte filelen,blocknum,blocktail,appendlen;
    u1byte buf[1024];//

    fp1=fopen(infile,"rb");
    if(fp1==NULL)
        return -1;//in file open error

    fp2=fopen(outfile,"wb");
    if(fp2==NULL)
        return -2;//out file open error

    fseek(fp1,0,SEEK_END);
    filelen=ftell(fp1);
    fseek(fp1,0,SEEK_SET);

    blocknum=filelen/16;//分块数量
    blocktail=filelen%16;//尾巴长度
    appendlen=16-blocktail;

    fwrite(&filelen,4,1,fp2);//文件长度放在头部

    lokicc_init(&a,MR_CBC,key,iv);

    for(int i=0;i<blocknum;i++){
        fread(buf,16,1,fp1);
        lokicc_encrypt(&a,buf);
        fwrite(buf,16,1,fp2);
    }

    if(blocktail!=0){
        for(int i=0;i<16;i++)
            buf[i]=appendlen;
        fread(buf,blocktail,1,fp1);
        lokicc_encrypt(&a,buf);
        fwrite(buf,16,1,fp2);
    }

    fclose(fp1);
    fclose(fp2);
}


//文件解密示例函数
int lokicc_decry_file(char infile[256],char outfile[256],u1byte key[32],u1byte iv[16]){
    lokicc a;
    FILE *fp1,*fp2;
    u4byte filelen,blocknum,blocktail;
    u1byte buf[1024];//

    fp1=fopen(infile,"rb");
    if(fp1==NULL)
        return -1;//in file open error

    fp2=fopen(outfile,"wb");
    if(fp2==NULL)
        return -2;//out file open error

    fread(&filelen,1,4,fp1);

    blocknum=filelen/16;//分块数量
    blocktail=filelen%16;//尾巴长度


    lokicc_init(&a,MR_CBC,key,iv);

    for(int i=0;i<blocknum;i++){
        fread(buf,1,16,fp1);
        lokicc_decrypt(&a,buf);
        fwrite(buf,1,16,fp2);
    }

    if(blocktail!=0){
        fread(buf,1,16,fp1);
        lokicc_decrypt(&a,buf);
        fwrite(buf,1,blocktail,fp2);
    }

    fclose(fp1);
    fclose(fp2);
}


void main()
{
    int i;
    unsigned int time1,time2;

    u4byte in_key[8]={1,0,0,0};
    u4byte in_blk[4]={0,0,0,0}, out_blk[4];
    int key_len = 256;
    lokicc a;
    u1byte key[32]={1,0,0,0,0,0};
    u1byte ming[16]={0};
    u1byte mi[16]={0},siv[16]={0};
    u1byte buf[4096]={0};



    printf("\n明文：");
    for (i=0;i<4;i++)
    {
        printf("%08x ",in_blk[i]);
    }
    printf("\n密钥长度：%d",key_len);
    printf("\n密钥：");
    for (i=0;i<4;i++)
    {
        printf("%08x ",in_key[i]);
    }

    set_key(in_key, key_len);

    time1=time(NULL);
    for (i=0;i<NUM;i++)
    {
        encrypt(in_blk, out_blk);
        in_blk[0]=out_blk[0];
    }
    time2=time(NULL);

    printf("\n密文：");
    for (i=0;i<4;i++)
    {
        printf("%08x ",out_blk[i]);
    }
    printf("共运行 0x%x 个分组\n",NUM);
    printf("运行速度为%f Mbps\n",(0.000128*NUM)/(time2-time1));


    decrypt(out_blk, in_blk);

    printf("\n解密后明文：");
    for (i=0;i<4;i++)
    {
        printf("%08x ",in_blk[i]);
    }
    printf("\n");

    lokicc_init(&a,MR_ECB,key,siv);
    lokicc_encrypt(&a,ming);
    printf("key 0,ming 0,out:\n");
    for (i=0;i<16;i++)
    {
        printf("%02x ",ming[i]);
    }
    printf("\n");
    lokicc_decrypt(&a,ming);

    for (i=0;i<16;i++)
    {
        printf("%02x ",ming[i]);
    }

    printf("\ncbc test\n");
    for(i=0;i<32;i++)key[i]=i;
    for(i=0;i<16;i++)siv[i]=i;

    lokicc_init(&a,MR_CBC,key,siv);

    for(i=0;i<256;i++)
        lokicc_encrypt(&a,(u1byte *)&buf[i*16]);

    for(i=0;i<256*16;i++)
        printf("%0x,",buf[i]);
    printf("\n\n");
    lokicc_init(&a,MR_CBC,key,siv);

    for(i=0;i<256;i++)
        lokicc_decrypt(&a,(u1byte *)&buf[i*16]);

    for(i=0;i<256*16;i++)
        printf("%0x,",buf[i]);
    printf("\n\n");

    for(int i=0;i<32;i++)key[i]=i*(i+i+1);
    for(int i=0;i<16;i++)siv[i]=i|key[i]+i*7;

    lokicc_encry_file("Wildlife.wmv","jiami.dat",key,siv);

    lokicc_decry_file("jiami.dat","jiemi.wmv",key,siv);


}



