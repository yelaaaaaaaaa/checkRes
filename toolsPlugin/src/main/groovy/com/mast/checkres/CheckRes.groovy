package com.mast.checkres

import com.google.gson.Gson
import org.gradle.api.Project

import java.util.concurrent.ConcurrentHashMap

class CheckRes {
    static def md5List = new ArrayList<String>()
    static def map = new ConcurrentHashMap<String, String>(400)
    static def diffMap = new HashMap<String, String>(400)

    static void createResTask(Project project) {
        diffMap.clear()
        md5List.clear()
        map.clear()
        Tiny tiny = project.extensions.getByName("tiny")
        TinyUtil tinify = new TinyUtil()
        tinify.init(tiny.tinifyId)
        project.task("checkres").doFirst {
            def projectPath = project.getAllprojects()
            for (projectc in projectPath) {
                def name = projectc.getName()
                Utils.printDebugmm("路径 {$name }")
            }
            def path = project.getProjectDir().getAbsolutePath() + "/src/main/res/"
            def outPath = project.getProjectDir().getAbsolutePath() + "/build/compress"
            def outPathFile = project.getProjectDir().getAbsolutePath() + "/compress.json"
            def diffFilePath = project.getRootDir().getAbsolutePath() + "/diff.json"
            try {
                File res = new File(path)
                if (res == null || !res.exists()) {
                    return
                }
            } catch (Exception e) {
                return
            }

            def json = new File(outPathFile)
            CompressBean compressBean
            if (!json.exists()) {
                json.createNewFile()
                compressBean = new CompressBean()
            } else {
                def jsonContent = FileUtils.getFileContent(json)
                compressBean = new Gson().fromJson(jsonContent, CompressBean.class)
            }
            def diffFile = new File(diffFilePath)
            if (!diffFile.exists()) {
                diffFile.createNewFile()
                diffMap.clear()
                md5List.clear()
            }
//            else if (project.getName() == "app") {
//                diffFile.createNewFile()
//                diffMap.clear()
//                md5List.clear()
//            }
            else {
                def jsonContent = FileUtils.getFileContent(diffFile)
                if (!jsonContent.isEmpty()){
                    try {
                        def diffBean = new Gson().fromJson(jsonContent, DiffBean.class)
                        if (diffBean != null) {
                            diffMap = diffBean.diffList
                        }
                    }catch(Exception e1){

                    }

                }

            }
            def out = new File(outPath)
            if (!out.exists()) {
                out.mkdirs()
            }
            File file = new File(path)
            if (compressBean == null) {
                compressBean = new CompressBean()
            }

            if (compressBean.data == null) {
                compressBean.data = new ArrayList<String>()
            }
            eachFolder(file, tinify, outPath, compressBean.data)
            def jsonResult = new Gson().toJson(compressBean)
            FileUtils.toFileContent(json, jsonResult)
            def diffBean = diffMap.clone()
            def diffResult = new Gson().toJson(new DiffBean(diffBean))
            //   Utils.printDebugmm("Diff:"+diffResult)
            FileUtils.toFileContent(diffFile, diffResult)
            Utils.printDebugmm("此次共压缩 {$TinyUtil.compressSize}")
            def finalMap = diffMap.clone()
            finalMap.each {
                if (it.key!=it.value) {
                    Utils.printDebugmm("重复文件")
                    Utils.printDebugmm(it.key)
                    Utils.printDebugmm(it.value)
                }
            }
            def size = finalMap.size()
            Utils.printDebugmm("此次共找到重复文件 {$size}")

        }

    }

    static void eachFolder(File file, TinyUtil tinyUtil, String outPath, ArrayList<String> list) {
        if (file.isDirectory()) {
            file.listFiles().each {
                eachFolder(it, tinyUtil, outPath, list)
            }
        } else {
            def md5 = FileUtils.getMD5(file.absolutePath)
            if (md5List.contains(md5) ) {
                if (map.get(md5) != file.absolutePath) {
                    diffMap.put(file.absolutePath, map.get(md5))
                }
            } else {
                md5List.add(md5)
                map.put(md5, file.absolutePath)
            }

            if (file.name.endsWith(".png")) {
                int size = file.size() / 1024
                if (size > 10) {
                    if (list.contains(file.absolutePath)) {
                        Utils.printDebugmm(file.absolutePath + "  已经压缩过了")
                    } else {
                        Utils.printDebugmm(file.absolutePath + "  size = " + size + "KB")
                        tinyUtil.compress(file.absolutePath, outPath, file.name, list)
                    }
                }
            }
        }
    }
}