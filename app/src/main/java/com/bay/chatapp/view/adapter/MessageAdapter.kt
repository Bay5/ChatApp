package com.bay.chatapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.data.entity.ChatMessage
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MessageAdapter(
    private var items: List<ChatMessage>,
    private val currentUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_ME = 1
        private const val VIEW_OTHER = 2
        private const val VIEW_DATE = 3
    }

    // internal flat list that includes date headers
    private var rows: List<Row> = emptyList()

    private sealed class Row {
        data class Header(val label: String) : Row()
        data class MessageRow(val msg: ChatMessage) : Row()
    }

    fun submitList(newItems: List<ChatMessage>) {
        items = newItems
        rows = buildRows(newItems)
        notifyDataSetChanged()
    }

    private fun buildRows(msgs: List<ChatMessage>): List<Row> {
        if (msgs.isEmpty()) return emptyList()

        // assume msgs already ordered ASC from ViewModel; if not, sort ASC
        val sorted = msgs.sortedBy { it.timestamp }
        val out = mutableListOf<Row>()

        val dayKeyFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dayLabelFmt = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        val nowDayKey = dayKeyFmt.format(Date())
        val yesterdayDayKey = dayKeyFmt.format(Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L))

        var currentDayKey: String? = null
        sorted.forEach { m ->
            val key = dayKeyFmt.format(Date(m.timestamp))
            if (key != currentDayKey) {
                val label = when (key) {
                    nowDayKey -> "Today"
                    yesterdayDayKey -> "Yesterday"
                    else -> dayLabelFmt.format(Date(m.timestamp))
                }
                out.add(Row.Header(label))
                currentDayKey = key
            }
            out.add(Row.MessageRow(m))
        }
        return out
    }

    override fun getItemViewType(position: Int): Int {
        return when (val row = rows[position]) {
            is Row.Header -> VIEW_DATE
            is Row.MessageRow -> if (row.msg.fromUid == currentUid) VIEW_ME else VIEW_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_ME -> MeViewHolder(inflater.inflate(R.layout.item_message_me, parent, false))
            VIEW_OTHER -> OtherViewHolder(inflater.inflate(R.layout.item_message_other, parent, false))
            else -> DateViewHolder(inflater.inflate(R.layout.item_date_header, parent, false))
        }
    }

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (holder as DateViewHolder).bind(row.label)
            is Row.MessageRow -> {
                val msg = row.msg
                if (holder is MeViewHolder) holder.bind(msg)
                else if (holder is OtherViewHolder) holder.bind(msg)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val fmt = SimpleDateFormat("HH.mm", Locale.getDefault())
        return fmt.format(Date(millis))
    }

    inner class MeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tvMessageMe)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeMe)
        private val tvCheck: TextView = itemView.findViewById(R.id.tvCheckMe)
        private val bubble: ConstraintLayout = itemView.findViewById(R.id.bubbleMe)
        fun bind(msg: ChatMessage) {
            tvMsg.text = msg.text
            tvTime.text = formatTime(msg.timestamp)
            tvCheck.text = when (msg.messageStatus) {
                "pending" -> "…"
                "sent" -> "✓"
                "read" -> "✓✓"
                else -> "✓"
            }
            tvMsg.post {
                val bottomCase = tvMsg.lineCount >= 2
                val cs = ConstraintSet()
                cs.clone(bubble)
                val margin4dp = (4 * itemView.resources.displayMetrics.density).roundToInt()

                if (!bottomCase) {
                    cs.clear(R.id.tvTimeMe, ConstraintSet.TOP)
                    cs.clear(R.id.tvCheckMe, ConstraintSet.TOP)

                    cs.connect(R.id.tvTimeMe, ConstraintSet.BASELINE, R.id.tvMessageMe, ConstraintSet.BASELINE)
                    cs.connect(R.id.tvCheckMe, ConstraintSet.BASELINE, R.id.tvMessageMe, ConstraintSet.BASELINE)

                    cs.connect(R.id.tvMessageMe, ConstraintSet.END, R.id.tvTimeMe, ConstraintSet.START, margin4dp)
                    cs.connect(R.id.tvTimeMe, ConstraintSet.END, R.id.tvCheckMe, ConstraintSet.START, margin4dp)
                    cs.connect(R.id.tvCheckMe, ConstraintSet.END, R.id.bubbleMe, ConstraintSet.END)
                } else {
                    cs.clear(R.id.tvTimeMe, ConstraintSet.BASELINE)
                    cs.clear(R.id.tvCheckMe, ConstraintSet.BASELINE)
                    // Remove any previous END-to-time constraint to avoid reserved space
                    cs.clear(R.id.tvMessageMe, ConstraintSet.END)

                    cs.connect(R.id.tvMessageMe, ConstraintSet.END, R.id.bubbleMe, ConstraintSet.END)

                    cs.connect(R.id.tvTimeMe, ConstraintSet.TOP, R.id.tvMessageMe, ConstraintSet.BOTTOM, margin4dp)
                    cs.connect(R.id.tvTimeMe, ConstraintSet.END, R.id.tvCheckMe, ConstraintSet.START, margin4dp)

                    cs.connect(R.id.tvCheckMe, ConstraintSet.TOP, R.id.tvMessageMe, ConstraintSet.BOTTOM, margin4dp)
                    cs.connect(R.id.tvCheckMe, ConstraintSet.END, R.id.bubbleMe, ConstraintSet.END)
                }

                cs.applyTo(bubble)
            }
        }
    }

    inner class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tvMessageOther)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeOther)
        private val bubble: ConstraintLayout = itemView.findViewById(R.id.bubbleOther)
        fun bind(msg: ChatMessage) {
            tvMsg.text = msg.text
            tvTime.text = formatTime(msg.timestamp)
            tvMsg.post {
                val bottomCase = tvMsg.lineCount >= 2
                val cs = ConstraintSet()
                cs.clone(bubble)
                val margin4dp = (4 * itemView.resources.displayMetrics.density).roundToInt()

                if (!bottomCase) {
                    cs.clear(R.id.tvTimeOther, ConstraintSet.TOP)
                    cs.connect(R.id.tvTimeOther, ConstraintSet.BASELINE, R.id.tvMessageOther, ConstraintSet.BASELINE)
                    cs.connect(R.id.tvMessageOther, ConstraintSet.END, R.id.tvTimeOther, ConstraintSet.START, margin4dp)
                    cs.connect(R.id.tvTimeOther, ConstraintSet.END, R.id.bubbleOther, ConstraintSet.END)
                } else {
                    cs.clear(R.id.tvTimeOther, ConstraintSet.BASELINE)
                    // Remove any previous END-to-time constraint to avoid reserved space
                    cs.clear(R.id.tvMessageOther, ConstraintSet.END)
                    cs.connect(R.id.tvMessageOther, ConstraintSet.END, R.id.bubbleOther, ConstraintSet.END)
                    cs.connect(R.id.tvTimeOther, ConstraintSet.TOP, R.id.tvMessageOther, ConstraintSet.BOTTOM, margin4dp)
                    cs.connect(R.id.tvTimeOther, ConstraintSet.END, R.id.bubbleOther, ConstraintSet.END)
                }

                cs.applyTo(bubble)
            }
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDateHeader)
        fun bind(label: String) {
            tvDate.text = label
        }
    }
}
