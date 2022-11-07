package com.mast.checkres

import com.tinify.AccountException
import com.tinify.Tinify

import java.nio.channels.FileChannel

class TinyUtil {
    public static int compressSize = 0

    void init(String id) {
        if (id == null || id == "") {
            throw new NoValueException("请传入tinify id")
        }
        try {
            compressSize = 0
            Tinify.setKey(id)
            Tinify.validate()
        } catch (AccountException exception) {
            println("TinyCompressor" + ex.printStackTrace())
        }
    }

    void compress(String path, String outPath, String name, ArrayList<String> list) {
        def before = getFileSize(path)
        Utils.printDebugmm("压缩前 size =  " + before)
        def tinySource = Tinify.fromFile(path)
        String outName = outPath + "/" + name
        tinySource.toFile(outName)
        def after = getFileSize(outName)
        Utils.printDebugmm("压缩后 size =  "+ after)
        compressSize += before - after
        def file =  new File(path)
        if (file.exists()){
            file.delete()
        }
        copyFileUsingFileChannels(new File(outName),file)
        Utils.printDebugmm("copy 完成")
        list.add(path)
    }

    int getFileSize(String path) {
        File file = new File(path)
        int size = file.size() / 1024
        return size
    }
    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
}