package dev.rayzr.gameboi.render

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.MemoryCacheImageOutputStream

class RenderContext(val channel: MessageChannel, width: Int, height: Int) {
    val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D
        get() = image.createGraphics()

    var lastMessage: Message? = null

    fun clear() {
        graphics.clearRect(0, 0, image.width, image.height)
    }

    fun renderText(text: String, x: Int, y: Int, size: Int = 20) {
        graphics.run {
            font = RenderUtils.font.deriveFont(size.toFloat())
            drawString(text, x, y)
        }
    }

    fun draw(callback: (Message) -> Unit = {}) {
        channel.sendFile(toJpeg(), "render.jpg").queue {
            lastMessage?.delete()?.queue()

            lastMessage = it
            callback.invoke(it)
        }
    }

    fun toJpeg(): ByteArray {
        val outputStream = ByteOutputStream()

        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        val params = writer.defaultWriteParam
        params.compressionMode = ImageWriteParam.MODE_EXPLICIT
        params.compressionQuality = 0.9f

        writer.output = MemoryCacheImageOutputStream(outputStream)
        writer.write(null, IIOImage(image, null, null), params)

        writer.dispose()
        outputStream.close()

        return outputStream.bytes!! // TODO: No '!!'?
    }
}