#include "lame-3.100_libmp3lame/lame.h"
#include "Mp3Encoder.h"

static lame_global_flags *lame = NULL;

JNIEXPORT void JNICALL Java_com_lzx_record_LameManager_init(
        JNIEnv *env,
        jclass cls,
        jint inSamplerate,
        jint inChannel,
        jint outSamplerate,
        jint outBitrate,
        jint quality){
    if(lame != NULL){
        lame_close(lame);
        lame = NULL;
    }

    lame =lame_init();
    //初始化，设置参数
    lame_set_in_samplerate(lame,inSamplerate);//输入采样率
    lame_set_out_samplerate(lame,outSamplerate);//输出采样率
    lame_set_num_channels(lame,inChannel);//声道
    lame_set_brate(lame,outBitrate);//比特率
    lame_set_quality(lame,quality);//质量
    lame_init_params(lame);
}

JNIEXPORT jint JNICALL Java_com_lzx_record_LameManager_encode(
        JNIEnv  *env,
        jclass cls,
        jshortArray buffer_left,
        jshortArray buffer_right,
        jint samples,
        jbyteArray mp3buf){

    //把Java传过来参数转成C中的参数进行修改
    jshort * j_buff_left =(*env)->GetShortArrayElements(env,buffer_left,NULL);
    jshort * j_buff_right = (*env) ->GetShortArrayElements(env,buffer_right,NULL);

    const jsize mp3buf_size = (*env) ->GetArrayLength(env,mp3buf);

    jbyte* j_mp3buff = (*env) ->GetByteArrayElements(env,mp3buf,NULL);

    int result = lame_encode_buffer(lame,j_buff_left,j_buff_right,samples,j_mp3buff,mp3buf_size);


    //释放参数
    (*env)->ReleaseShortArrayElements(env,buffer_left,j_buff_left,0);
    (*env)->ReleaseShortArrayElements(env,buffer_right,j_buff_right,0);
    (*env)->ReleaseByteArrayElements(env,mp3buf,j_mp3buff,0);
    return result;

}

JNIEXPORT jint JNICALL Java_com_lzx_record_LameManager_encodeInterleaved(
        JNIEnv  *env,
        jclass cls,
        jshortArray pcm_buffer,
        jint samples,
        jbyteArray mp3buf){

    jshort * j_pcm_buffer =(*env)->GetShortArrayElements(env,pcm_buffer,NULL);

    const jsize mp3buf_size = (*env) ->GetArrayLength(env,mp3buf);

    jbyte* j_mp3buff = (*env) ->GetByteArrayElements(env,mp3buf,NULL);

    int result =  lame_encode_buffer_interleaved(lame,j_pcm_buffer,samples,j_mp3buff,mp3buf_size);

    //释放参数
    (*env)->ReleaseShortArrayElements(env,pcm_buffer,j_pcm_buffer,0);
    (*env)->ReleaseByteArrayElements(env,mp3buf,j_mp3buff,0);
    return result;

}

JNIEXPORT jint JNICALL Java_com_lzx_record_LameManager_encodeByByte(
        JNIEnv  *env,
        jclass cls,
        jbyteArray buffer_left,
        jbyteArray buffer_right,
        jint samples,
        jbyteArray mp3buf){

    jbyte * j_buff_left =(*env)->GetByteArrayElements(env,buffer_left,NULL);
    jbyte * j_buff_right = (*env) ->GetByteArrayElements(env,buffer_right,NULL);

    const jsize mp3buf_size = (*env) ->GetArrayLength(env,mp3buf);

    jbyte* j_mp3buff = (*env) ->GetByteArrayElements(env,mp3buf,NULL);

    int result = lame_encode_buffer(lame, (const short *) j_buff_left, (const short *) j_buff_right, samples, j_mp3buff, mp3buf_size);

    //释放参数
    (*env)->ReleaseByteArrayElements(env,buffer_left,j_buff_left,0);
    (*env)->ReleaseByteArrayElements(env,buffer_right,j_buff_right,0);
    (*env)->ReleaseByteArrayElements(env,mp3buf,j_mp3buff,0);
    return result;

}

JNIEXPORT jint JNICALL Java_com_lzx_record_LameManager_flush(
        JNIEnv *env,
        jclass cls,
        jbyteArray mp3buf){
    const jsize mp3buf_size = (*env) ->GetArrayLength(env,mp3buf);

    jbyte* j_mp3buff = (*env) ->GetByteArrayElements(env,mp3buf,NULL);

    int result = lame_encode_flush(lame,j_mp3buff,mp3buf_size);
    //释放
    (*env)->ReleaseByteArrayElements(env,mp3buf,j_mp3buff,0);

    return result;
}

JNIEXPORT void JNICALL Java_com_lzx_record_LameManager_close(
        JNIEnv *env,
        jclass cls){
    lame_close(lame);
    lame =NULL;
}
