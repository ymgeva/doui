# doUI
You know how it is when you and your husband / wife alwasy argu about who was supposed to do what and when?
You always have to remind you partner (or the other way way around) that it's his day to pick up the kids,
or that you already told them 1000 times that today's yuor dentist appointmant and you'l be home late, but they keep forgetting...

Well doUI is about solving this issue for good.

The couple can create a time schedule and task list for each other, 
set up alarms to notify their partner when something needs to be done,
get a notification when something was actually done.
In addition there a shared shopping list for things.

Note - for the App to work properly you need to register 2 users and connect them through the login proccess.

# Login
The app works with the parse.com backend.
The initial login is done with the LoginActivity, and has 2 phases - 
  - Login or Sign Up with a new user.
  - Connect to a partner. 
The first member of the couple to create the account enters his partners mail and a shared secret.
This secrete is sent to the partners email.
Wheh the second member signs up they need to enter their partner's email and the shared secret that was sent to them.

# Task List
The task list shows the tasks that needs to be done, sorted by date.
Each tasks has an icon - "i" for things I need to do or "u" for things my partner needs to do.
Clicking a task shows it's details which can be editted (by an edit button), or marked as done.
You can also mark as done by swiping in list.
Adding a new task requires entring the following - Title, date, time and who needs to do it.

# Shopping list 
Shopping list items are ordered by importance. 
Items can be editted by clicking on them and pressing the edit button in the action bar.

# Sync
The data is stored locally with a Contect Provider.
Sync is done via a sync adapter, with a set interval of 3 hours.
Sync is also done when the device gets a push notification for - 
  - Urgent task (new / changed task that happens today)
  - Urgent shopping item.
  - Notifcation for task done (if needed).
  - Completion of partners handshake proccess.
Also, pull to refresh and changes made to items in both lists starts an immdiate sync (of that table only).

# Notifications
NotificatinService handles notification for remniders of taks in the appropriate set time and for urgent tasks / shoppig list item changes.



