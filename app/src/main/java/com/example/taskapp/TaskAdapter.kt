package com.example.taskapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.database.Task
import com.example.taskapp.databinding.OneTaskBinding

class TaskAdapter(private val listener: OnItemClickListener): ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(private val binding: OneTaskBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION)
                    {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }

                taskCheck.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION)
                    {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, taskCheck.isChecked)
                    }
                }
            }
        }
        fun bind(task:Task)
        {
            binding.apply {
                taskCheck.isChecked = task.completed
                taskText.text = task.name
                taskImage.isVisible = task.important
            }
        }
    }

    interface OnItemClickListener {
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
        fun onItemClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = OneTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    fun getTaskAtPosition(position: Int): Task? {
        return getItem(position)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>()
    {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

}