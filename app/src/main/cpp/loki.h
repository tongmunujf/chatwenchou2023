//
// Created by iiesm on 2023/8/24.
//

#ifndef CHAT_LOKI_H
#define CHAT_LOKI_H
typedef unsigned char   u1byte; /* an 8 bit unsigned character type */

typedef unsigned short  u2byte; /* a 16 bit unsigned integer type   */

typedef unsigned long   u4byte; /* a 32 bit unsigned integer type   */



typedef signed char     s1byte; /* an 8 bit signed character type   */

typedef signed short    s2byte; /* a 16 bit signed integer type     */

typedef signed long     s4byte; /* a 32 bit signed integer type     */







#ifdef  __cplusplus

extern "C"

    {

#endif



char **cipher_name(void);

u4byte *set_key(const u4byte in_key[], const u4byte key_len);

void encrypt(const u4byte in_blk[4], u4byte out_blk[4]);

void decrypt(const u4byte in_blk[4], u4byte out_blk[4]);



#ifdef  __cplusplus

};

#endif


#ifdef _MSC_VER

#  include <stdlib.h>

#  pragma intrinsic(_lrotr,_lrotl)

#  define rotr(x,n) _lrotr(x,n)

#  define rotl(x,n) _lrotl(x,n)


#else



#define rotr(x,n)   (((x) >> ((int)(n))) | ((x) << (32 - (int)(n))))

#define rotl(x,n)   (((x) << ((int)(n))) | ((x) >> (32 - (int)(n))))

#endif
#define bswap(x)    (rotl(x, 8) & 0x00ff00ff | rotr(x, 8) & 0xff00ff00)
#define byte(x,n)   ((u1byte)((x) >> (8 * n)))


#endif //CHAT_LOKI_H
