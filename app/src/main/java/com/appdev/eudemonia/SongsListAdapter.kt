import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.MyExoplayer
import com.appdev.eudemonia.PlayerActivity
import com.appdev.eudemonia.databinding.SongListItemRecyclerRowBinding
import com.appdev.eudemonia.models.SongModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class SongsListAdapter(private val songIdList: List<String>) : RecyclerView.Adapter<SongsListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) : RecyclerView
        .ViewHolder(binding.root) {

        fun bindData(songId: String) {
            FirebaseFirestore.getInstance().collection("songs").document(songId).get()
                .addOnSuccessListener {
                val song = it.toObject(SongModel::class.java)
                song?.apply{
                    binding.songTitleTextView.text = title
                    binding.songSubtitleTextView.text = subtitle
                    //images to add later
                    Glide.with(binding.songCoverImageView).load(coverUrl).apply(RequestOptions()
                        .transform(RoundedCorners(32)))
                        .into(binding.songCoverImageView)
                    binding.root.setOnClickListener{
                        MyExoplayer.startPlaying(binding.root.context,song)
                        it.context.startService(Intent(it.context, PlayerActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songIdList[position])
    }

    override fun getItemCount(): Int{
        return songIdList.size
    }
}
