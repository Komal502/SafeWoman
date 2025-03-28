import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.safewoman.R

class ImageSliderAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    // ViewHolder to hold the ImageView
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
    }

    // Inflates the layout for each slider item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return ImageViewHolder(view)
    }

    // Binds the image to the view holder
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // Safely access the image resource using getOrNull
        val imageResource = images.getOrNull(position)

        // Set the image if available, else set a default placeholder
        if (imageResource != null) {
            holder.imageView.setImageResource(imageResource)
        } else {
            // Default image in case the resource is null or unavailable
            holder.imageView.setImageResource(R.drawable.w1) // Placeholder image
        }
    }

    // Returns the total number of items in the dataset
    override fun getItemCount(): Int = images.size
}
