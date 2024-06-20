package com.example.tasker

import android.net.Uri

data class AttachmentElements(
    var fileType:Int? = null,
    var fileName:String? = null,
    var filePath:String? = null,
){
    companion object{
        val PDF = R.drawable.pdf
        val IMAGE = R.drawable.image
        val VIDEO = R.drawable.video_editing
        val PPT = R.drawable.ppt_file
        val DOCUMENT = R.drawable.document
        val EXCEL = R.drawable.xls
        val TEXT = R.drawable.txt
        val AUDIO = R.drawable.sound_waves
        val UNKNOWN = R.drawable.blank_page


        const val NAME_KEY = "fileName"
        const val PATH_KEY = "filePath"
        const val TYPE_KEY = "fileType"



        fun findAttachmentIcon(file:String):Int{
            when{
                file.matches(".*\\.(png|jpeg|jpg|svg|bmp|gif|webp|heif|tiff|tif|heic|raw|cr2|nef|orf|sr2)$".toRegex())-> {
                    return IMAGE
                }
                file.matches(".*.(pdf)$".toRegex())->{
                    return PDF
                }
                file.matches(".*.(mp4|webm|avi|mov|wmv|flv|f4v|ogv|m4v|3gp|3g2|avchd|mkv)$".toRegex())->{
                    return VIDEO
                }
                file.matches(".*\\.(doc|docx|odt)$".toRegex())->{
                    return DOCUMENT
                }
                file.matches(".*\\.(mp3|aac|ogg|wav|mqa|wma|aiff|alac|dsd|flac)$".toRegex())->{
                    return AUDIO
                }
                file.matches(".*\\.(ppt|pptx|odp)$".toRegex())->{
                    return PPT
                }
                file.matches(".*\\.(xls|xlsx)$".toRegex())->{
                    return EXCEL
                }
                file.matches(".*\\.(txt|rtf|md|msg|wps)$".toRegex())->{
                    return TEXT
                }
            }
            return UNKNOWN
        }
    }
}
