LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= gif_V1_0
LOCAL_SRC_FILES := gif.c \
    giflib\dgif_lib.c \
    giflib\gif_hash.c \
    giflib\gifalloc.c \

include $(BUILD_SHARED_LIBRARY)
