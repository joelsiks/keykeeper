package structure

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.application.keykeeper.R
import kotlinx.coroutines.*
import java.net.URL

class CredentialsAdapter(
    context: Context,
    @LayoutRes private val layoutResource: Int,
    private val items: List<CredentialsItem>
): ArrayAdapter<CredentialsItem>(context, layoutResource, items) {
    fun addAll(collection: Collection<CredentialsItem>, callback: (collection: Collection<CredentialsItem>) -> Unit) {
        // Load images before adding
        CoroutineScope(Dispatchers.Default).launch {
            collection.forEach {
                it.image = getImageFromUrl(it.uri)
                withContext(Dispatchers.Main) {
                    super.add(it)
                }
            }
            callback(collection)
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?: LayoutInflater.from(context).inflate(layoutResource, parent, false)
        val item = getItem(position)
        val titleLabel = view.findViewById<TextView>(R.id.storage_item_title_label)
        val urlLabel = view.findViewById<TextView>(R.id.storage_item_url_label)
        val image = view.findViewById<ImageView>(R.id.storage_item_image)
        item?.let {
            titleLabel.text = item.label
            urlLabel.text = item.uri
            image.setImageDrawable(item.image)
        }

        return view
    }
    private fun getImageFromUrl(urlString: String?): Drawable? {
        return try {
            // Match for protocol and site
            val groups = Regex("(.*)://([^/]*)/?").find(urlString!!)?.groups!!
            val protocol = groups[1]?.value
            val site = groups[2]?.value
            val url = URL(protocol, site, "/favicon.ico")
            val connection = url.openConnection()
            connection.connectTimeout = 300
            Drawable.createFromStream(connection.getInputStream(), urlString)
        } catch (e: Exception) {
            Log.e("getIconFromUrl", e.toString() + ' ' + e.message)
            null
        }
    }
     fun getItems(): List<CredentialsItem> {
        return this.items
     }
}