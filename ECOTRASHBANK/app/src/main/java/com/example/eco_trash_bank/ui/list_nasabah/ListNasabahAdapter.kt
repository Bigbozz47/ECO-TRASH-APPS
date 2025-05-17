import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.databinding.ItemNasabahBinding
import com.example.eco_trash_bank.ui.list_nasabah.Nasabah

class ListNasabahAdapter(
    private val list: List<Nasabah>,
    private val onDetailClick: (Nasabah) -> Unit
) : RecyclerView.Adapter<ListNasabahAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNasabahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nasabah: Nasabah) {
            binding.tvName.text = nasabah.name
            binding.tvEmail.text = nasabah.email
            binding.tvSaldo.text = "Saldo: Rp ${nasabah.saldo}"
            binding.tvPoin.text = "Poin: ${nasabah.poin}"
            binding.btnDetail.setOnClickListener {
                onDetailClick(nasabah)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNasabahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}
