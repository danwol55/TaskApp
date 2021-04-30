package com.example.taskapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.R.id.transition_first
import com.example.taskapp.database.*
import com.example.taskapp.databinding.ActivityMainBinding
import com.example.taskapp.utilities.onQueryTextChange
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.one_task.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TaskAdapter.OnItemClickListener,
    NavigationView.OnNavigationItemSelectedListener, NoticeDialog.NoticeDialogListener
{
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var motionLayout: MotionLayout
    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var sharedPref: SharedPreferences
    private lateinit var alarmManager: AlarmManager
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var editText: EditText
    private lateinit var clickedTask: Task
    private lateinit var calendar: Calendar
    private lateinit var reminder: Reminder
    private var onTaskClicked = false
    var addTaskClicked = false
    var firstTransition = true

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawerLayout = findViewById(R.id.drawer_layout)
        notificationManager = NotificationManagerCompat.from(this)
        notificationHelper = NotificationHelper(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        editText = findViewById(R.id.task_edit)
        sharedPref = getPreferences(MODE_PRIVATE)
        motionLayout = findViewById(R.id.motion_layout)

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var isImportant = false
        var hasReminder = false

        taskAdapter = TaskAdapter(this)
        navController = NavController(this)
        setSupportActionBar(toolbar)

        navigationView.setupWithNavController(navController)
        navigationView.setNavigationItemSelectedListener(this)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.drawer_layout))
        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        setupActionBarWithNavController(navController, appBarConfiguration)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)


        binding.apply {
            recyclerTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
            }

            toolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            motionLayout.setTransitionListener(object : MotionLayout.TransitionListener
            {
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int)
                {
                }

                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float)
                {
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int)
                {
                    if (firstTransition && addTaskClicked)
                    {
                        if (motionLayout.currentState == motionLayout.endState)
                        {
                            editText.requestFocus()
                            inputMethodManager.showSoftInput(editText, 0)
                            addTaskClicked = false

                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        }

                    }
                    if (motionLayout.currentState == motionLayout.startState)
                    {
                        task_add.visibility = FloatingActionButton.VISIBLE
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                    else if (motionLayout.currentState == motionLayout.endState)
                    {
                        task_add.visibility = FloatingActionButton.INVISIBLE
                    }
                }

                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float)
                {
                }

            })

            //Reminder Implementation/////////////////////////////////////////////////

            buttonRemind.setOnClickListener {
                motionLayout.setTransition(R.id.transition_second)
                motionLayout.transitionToEnd()
                firstTransition = false

                val calendar1 = Calendar.getInstance()
                val hour1 = calendar1.get(Calendar.HOUR)
                val minute1 = calendar1.get(Calendar.MINUTE)
                val day1 = calendar1.get(Calendar.DAY_OF_MONTH)
                val month1 = calendar1.get(Calendar.MONTH)

                if (onTaskClicked)
                {
                    if (clickedTask.reminder.enabled)
                    {
                        val reminder = clickedTask.reminder
                        hourPicker.value = reminder.hour
                        minutePicker.value = reminder.minute
                        dayPicker.value = reminder.day
                        monthPicker.value = reminder.month
                        reminderDelete.isEnabled = true
                    }
                    else
                    {
                        hourPicker.value = hour1
                        minutePicker.value = minute1
                        dayPicker.value = day1
                        monthPicker.value = month1
                        updateLabel(calendar1)
                    }
                }
                else
                {
                    hourPicker.value = hour1
                    minutePicker.value = minute1
                    dayPicker.value = day1
                    monthPicker.value = month1
                    updateLabel(calendar1)
                }
            }

            reminderConfirm.setOnClickListener {
                //read input from number pickers
                val hour1 = hourPicker.value
                val minute1 = minutePicker.value
                val day1 = dayPicker.value
                val month1 = monthPicker.value

                //set the calendar
                calendar.set(Calendar.MONTH, month1)
                calendar.set(Calendar.DAY_OF_MONTH, day1)
                calendar.set(Calendar.HOUR_OF_DAY, hour1)
                calendar.set(Calendar.MINUTE, minute1)
                calendar.set(Calendar.SECOND, 0)

                //if task which have reminder was clicked
                if (onTaskClicked && clickedTask.reminder.enabled)
                {
                    cancelReminder(clickedTask.pendingIntentIndex)
                    Toast.makeText(this@MainActivity, "reminder updated", Toast.LENGTH_SHORT).show()
                }

                hasReminder = true
                reminder = Reminder(true, hour1, minute1, day1, month1)

                //changing the button text color
                //making layout transitions after click
                buttonRemind.setTextColor(Color.BLUE)
                motionLayout.setTransition(R.id.transition_second)
                motionLayout.transitionToStart()
                motionLayout.setTransition(transition_first)
                motionLayout.transitionToEnd()
            }

            reminderCancel.setOnClickListener {
                motionLayout.setTransition(R.id.transition_second)
                motionLayout.transitionToStart()
                motionLayout.setTransition(transition_first)
                motionLayout.transitionToEnd()
            }
            reminderDelete.setOnClickListener {

                val index = clickedTask.pendingIntentIndex
                cancelReminder(index)

                val task = clickedTask.copy(
                    reminder = Reminder(false, 0, 0, 0, 0),
                    pendingIntentIndex = -1
                )
                taskViewModel.updateTask(task)
                reminderDelete.isEnabled = false

                Toast.makeText(this@MainActivity, "reminder deleted", Toast.LENGTH_SHORT).show()
                buttonRemind.setTextColor(resources.getColor(R.color.design_default_color_primary))
            }

            //Reminder Implementation/////////////////////////////////////////////////

            //Pickers Implementation//////////////////////////////////////////////////

            hourPicker.apply {
                minValue = 0
                maxValue = hours.size - 1
                displayedValues = hours
                wrapSelectorWheel = true
                value = hour
            }

            hourPicker.setOnValueChangedListener { p0, p1, p2 ->
                calendar.set(Calendar.HOUR, hourPicker.value)
                updateLabel(calendar)
            }

            minutePicker.apply {
                minValue = 0
                maxValue = minutes.size - 1
                displayedValues = minutes
                wrapSelectorWheel = true
                value = minute
            }

            minutePicker.setOnValueChangedListener { p0, p1, p2 ->
                calendar.set(Calendar.MINUTE, minutePicker.value)
                updateLabel(calendar)
            }

            dayPicker.apply {
                minValue = 0
                maxValue = days.size - 1
                displayedValues = days
                wrapSelectorWheel = true
                value = day
            }

            dayPicker.setOnValueChangedListener { p0, p1, p2 ->
                calendar.set(Calendar.DAY_OF_MONTH, dayPicker.value)
                updateLabel(calendar)
            }

            monthPicker.apply {
                minValue = 0
                maxValue = months.size - 1
                displayedValues = months
                wrapSelectorWheel = true
                value = month
            }

            monthPicker.setOnValueChangedListener { p0, p1, p2 ->
                calendar.set(Calendar.MONTH, monthPicker.value)
                updateLabel(calendar)
            }

            //Pickers Implementation//////////////////////////////////////////////////

            //Buttons Implementation//////////////////////////////////////////////////

            buttonImportance.setOnClickListener {
                isImportant = if (isImportant)
                {
                    buttonImportance.setTextColor(resources.getColor(R.color.design_default_color_primary))
                    false
                }
                else
                {
                    buttonImportance.setTextColor(Color.BLUE)
                    true
                }
            }

            buttonConfirm.setOnClickListener {
                val task: Task?

                if (taskEdit.text.isNotEmpty())
                {
                    //get the text from field
                    val text = taskEdit.text.toString()
                    //TaskBuilder instance to create task
                    val taskBuilder = TaskBuilder(this@MainActivity)
                    //if task has reminder execute code below
                    if (hasReminder)
                    {
                        val requestCode = sharedPref.all.size
                        task = taskBuilder.createTaskWithReminder(text, reminder, isImportant, requestCode)
                        sharedPref.edit().putInt("$requestCode", requestCode).apply()
                        setReminder(calendar, taskBuilder.pendingIntent)
                        println(sharedPref.all)
                    }
                    //if task hasn't reminder execute code below
                    else task = taskBuilder.createTaskWithoutReminder(text, isImportant)
                    //if new task was created
                    if (!onTaskClicked) task.let { it1 -> taskViewModel.addTask(it1) }
                    //if existing task was clicked
                    else task.let { it1 -> taskViewModel.updateTask(it1) }

                    //restoring default state buttons colors
                    buttonImportance.setTextColor(resources.getColor(R.color.design_default_color_primary))
                    buttonRemind.setTextColor(resources.getColor(R.color.design_default_color_primary))
                    //hide task edit layout
                    motionLayout.setTransition(transition_first)
                    motionLayout.transitionToStart()
                    //restoring default states
                    editText.text.clear()
                    onTaskClicked = false
                    hasReminder = false
                    isImportant = false
                }
                else
                {
                    val snackbar = Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        "type the task name",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            task_add.setOnClickListener {
                motionLayout.setTransition(transition_first)
                motionLayout.transitionToEnd()

                buttonImportance.setTextColor(resources.getColor(R.color.design_default_color_primary))
                buttonRemind.setTextColor(resources.getColor(R.color.design_default_color_primary))

                firstTransition = true
                onTaskClicked = false
                addTaskClicked = true

                if (editText.text.isNotEmpty())
                {
                    editText.text.clear()
                }
            }

            //Buttons Implementation//////////////////////////////////////////////////

            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean
                {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
                {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    taskViewModel.deleteTask(task)
                    cancelReminder(task.pendingIntentIndex)
                }

            }).attachToRecyclerView(recyclerTasks)
        }

        taskViewModel.tasks.observe(this)
        {
            taskAdapter.submitList(it)
        }

    }

    private fun updateLabel(c: Calendar)
    {
        val format = "hh:mm dd/MM/yy"
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        time_view.text = simpleDateFormat.format(c.time)
    }

    private fun setReminder(c: Calendar, p: PendingIntent?)
    {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, p)
    }

    private fun cancelReminder(i: Int)
    {
        val intent = Intent(this@MainActivity, Receiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this@MainActivity, i, intent, 0)
        sharedPref.edit().remove(i.toString()).apply()
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        println(sharedPref.all)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.task_menu, menu)

        val searchItem = menu?.findItem(R.id.task_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextChange {
            taskViewModel.searchTask.value = it
        }

        lifecycleScope.launch {
            menu.findItem(R.id.task_hide_completed).isChecked =
                taskViewModel.preferencesFlow.first().hideCompleted
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean
    {
        var isAtLeastOneTask = false
        val tasksSize = taskViewModel.tasks.value?.size
        if (tasksSize != null && tasksSize >= 1) isAtLeastOneTask = true
        val deleteTasks = menu?.findItem(R.id.task_delete_all)
        deleteTasks?.isEnabled = isAtLeastOneTask
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.sort_by_name ->
            {
                taskViewModel.onSortOrder(SortOrder.SORT_BY_NAME)
                return true
            }
            R.id.sort_by_date ->
            {
                taskViewModel.onSortOrder(SortOrder.SORT_BY_DATE)
                return true
            }
            R.id.task_search -> true
            R.id.task_hide_completed ->
            {
                item.isChecked = !item.isChecked
                taskViewModel.onHideCompleted(item.isChecked)
                return true
            }
            R.id.task_delete_all ->
            {
                openAlertDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun openAlertDialog()
    {
        val dialog = NoticeDialog()
        dialog.show(supportFragmentManager, "dialog")
    }

    override fun onBackPressed()
    {
        when
        {
            motionLayout.currentState == motionLayout.endState -> {
                motionLayout.transitionToStart()
            }
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> super.onBackPressed()
        }
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean)
    {
        taskViewModel.onTaskCheck(task, isChecked)
    }

    override fun onItemClick(task: Task)
    {
        motionLayout.setTransition(transition_first)
        motionLayout.transitionToEnd()
        firstTransition = true
        onTaskClicked = true

        if (task.name.isNotEmpty()) editText.setText(task.name)
        if (task.important) button_importance.setTextColor(Color.BLUE)
        if (task.reminder.enabled) button_remind.setTextColor(Color.BLUE)
        clickedTask = task

    }


    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.completed_tasks ->
            {
                taskViewModel.onShowCompleted(true)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
        }
        return true
    }

    override fun onDialogPositiveClick(dialog: DialogFragment?)
    {
        taskViewModel.deleteAllTasks()
        val preferencesKeys = sharedPref.all.keys
        for(i in preferencesKeys)
        {
            val intent = Intent(this@MainActivity, Receiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(this@MainActivity, i.toInt(), intent, 0)
            sharedPref.edit().remove(i).apply()
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        println(sharedPref.all)
    }

}