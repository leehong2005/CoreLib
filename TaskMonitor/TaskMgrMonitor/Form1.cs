using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Collections;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace TaskMgrMonitor
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            this.pnlCanvas.Paint += new PaintEventHandler(pnlCanvas_Paint);
            myDelegate = new SendMessage(ProccessString);
            this.FormClosing += new FormClosingEventHandler(Form1_FormClosing);
        }

        void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            this.StopMonitor();
        }

        public void StopMonitor()
        {
            if (tMonitor != null)
            {
                UdpClient udpClient = new UdpClient("127.0.0.1", 1113);
                Byte[] sendBytes = Encoding.ASCII.GetBytes("quit");
                try
                {
                    udpClient.Send(sendBytes, sendBytes.Length);
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.ToString());
                }
                udpClient.Close();
                tMonitor.Join();
                tMonitor = null;
            }
        }

        public delegate void SendMessage(String myString);
        public SendMessage myDelegate;


        string receiveMessage = "";

        public void ProccessString(String buffer)
        {

            receiveMessage = buffer;

            if (string.IsNullOrEmpty(receiveMessage))
            {
                return;
            }
            try
            {
                string[] vals = receiveMessage.Split(new string[] { "##" }, StringSplitOptions.RemoveEmptyEntries);
                string taskmgrName = vals[0];
                string taskName = vals[1];

                TaskBean tb = this.GetTask(taskmgrName, taskName);
                tb.status = vals[2];
                tb.userinfo = vals[3];
            }
            catch (System.Exception ex)
            {
                return;
            }


            this.pnlCanvas.Invalidate();
            
        }

        public TaskBean GetTask(string mgrname, string taskname)
        {
            int mgrcount = taskMgrs.Count;
            bool isFind = false;
            TaskBean tb = null;
            for (int i = 0; i < mgrcount; i++)
            {
                TaskMgrBean tmb = (TaskMgrBean)taskMgrs[i];
                if (tmb.name.Equals(mgrname))
                {
                    isFind = true;
                    tb = new TaskBean();
                    tb.name = taskname;
                    tb.status = tb.userinfo = "";
                    tmb.tasks.Add(tb);
                    break;
                }
            }
            if ( !isFind )
            {
                TaskMgrBean tmb = new TaskMgrBean();
                tmb.name = mgrname;
                tmb.status = tmb.userinfo = "";

                taskMgrs.Add(tmb);

                tb = new TaskBean();
                tb.name = taskname;
                tb.status = tb.userinfo = "";
                tmb.tasks.Add(tb);
                tmb.height = curHeight;

            }
            return tb;
        }

        public Thread tMonitor = null;
        private void btnMonitor_Click(object sender, EventArgs e)
        {
            if (tMonitor != null)
            {
                StopMonitor();
                this.btnMonitor.Text = "Monitor";
            }
            else
            {
                tMonitor = new Thread(new ThreadStart(ThreadProc));

                tMonitor.Start();
                this.btnMonitor.Text = "Monitoring...";

            }
        }

       

        public void ThreadProc()
        {
            IPEndPoint ipp = new IPEndPoint(IPAddress.Any, 0);
            UdpClient u = new UdpClient(1113);

            while (true)
            {
                Byte[] receiveBytes = u.Receive(ref ipp);

                string returnData = Encoding.ASCII.GetString(receiveBytes);

                if (returnData.Equals("quit"))
                {
                    u.Close();
                    break;
                }

                this.BeginInvoke(myDelegate, new object[] { returnData });
            }

        }

        public class TaskBean
        {
            public string name;
            public string status;
            public string userinfo;
            public float height;
            public DateTime time;
        }

        public class TaskMgrBean
        {
            public ArrayList tasks = new ArrayList();
            public string name;
            public string status;
            public string userinfo;
            public float height;
            public DateTime time;
        }

        public float curHeight = 0.0f;
        public float curWidth = 0.0f;


        private ArrayList taskMgrs = new ArrayList();
        
        Font ftTaskMgr =  new Font("Microsoft Sans Serif", 8.25f + 4.0f,
                FontStyle.Regular, GraphicsUnit.Point);
        Font ftTask = new Font("Microsoft Sans Serif", 8.25f,
                FontStyle.Regular, GraphicsUnit.Point);

        SolidBrush brhTaskMgr = new SolidBrush(Color.Red);
        SolidBrush brhTasks = new SolidBrush(Color.Blue);

        public void DrawTask(Graphics grap)
        {
            grap.Clear(Color.White);

            float w = this.pnlCanvas.Width * 1.0f;
            float h = this.pnlCanvas.Height * 1.0f;

            int count = taskMgrs.Count;
            for (int i = 0; i < count; i++)
            {
                float x = cellWidth * i + 10;

                TaskMgrBean tmb = (TaskMgrBean)taskMgrs[i];

                float tmby = tmb.height + 10;

                if (tmby + 5 >= offsetY && tmby - 5 < offsetY + h || curHeight < h)
                {
                    if (x + cellWidth >= offsetX && x - cellWidth < offsetX + w || curWidth < w)
                    {
                        grap.DrawString(String.Format("{0} {1} {2}", tmb.name, tmb.status, tmb.userinfo),
                            ftTaskMgr,
                            brhTaskMgr,
                            x - offsetX, tmby - offsetY);
                    }

                }

                int taskcount = tmb.tasks.Count;
                for (int j = 0; j < taskcount; j++ )
                {
                    TaskBean tb = (TaskBean)tmb.tasks[j];

                    float y = 30 * j + 20 + 10 + tmb.height;

                    if ( y + 5 >=  offsetY && y - 5 < offsetY + h  || curHeight < h)
                    {
                        if (x + cellWidth >= offsetX && x - cellWidth < offsetX + w || curWidth < w)
                        {
                            grap.DrawString(String.Format("{0} {1} {2}", tb.name, tb.status, tb.userinfo),
                                ftTask,
                                brhTasks,
                                x - offsetX, y - offsetY);

                            if (tb.status.IndexOf("RUNNING") != -1)
                            {
                                grap.DrawLine(new Pen(new SolidBrush(Color.Blue)), x - offsetX, y + ftTask.Height / 2.0f - offsetY, x - offsetX, y + ftTask.Height / 2.0f + 15 - offsetY);
                            }

                            if (tb.status.IndexOf("FINISHED") != -1)
                            {
                                grap.DrawLine(new Pen(new SolidBrush(Color.Blue)), x - offsetX, y + ftTask.Height / 2.0f - offsetY, x - offsetX, y + ftTask.Height / 2.0f - 15 - offsetY);
                            }

                        }

                    }

                    if (j == taskcount - 1)
                    {
                        curHeight = 30 * j + 20 + tmb.height;
                    }
                    realH = curHeight + 10;
                }

                if ( i == count - 1 )
                {
                    curWidth = cellWidth * i;
                    realW = curWidth + 10;
                }
            }

            if (realH > h)
            {
                if (this.vsbPanel.Enabled == false)
                {
                    this.vsbPanel.Enabled = true;
                }
                this.vsbPanel.Maximum = (int)(realH + 0.5f);
            }

            if ( realW > w )
            {
                if (this.hsbPanel.Enabled == false)
                {
                    this.hsbPanel.Enabled = true;
                }
                this.hsbPanel.Maximum = (int)(realW + 0.5f);
            }
        }

        private void btnClear_Click(object sender, EventArgs e)
        {
            this.taskMgrs.Clear();
            this.curHeight = 0f;
            this.curWidth = 0f;
            this.realH = 0f;
            this.realW = 0f;
            this.vsbPanel.Maximum = this.pnlCanvas.Height;
            this.vsbPanel.Value = 1;
            this.hsbPanel.Maximum = this.pnlCanvas.Width;
            this.hsbPanel.Value = 1;
            this.pnlCanvas.Invalidate();
        }
        
        void pnlCanvas_Paint(object sender, PaintEventArgs e)
        {
            this.DrawTask(e.Graphics);
        }

        private void btnRefresh_Click(object sender, EventArgs e)
        {
            this.pnlCanvas.Invalidate();
            this.pnlCanvas.Update();
        }

        private void vsbPanel_ValueChanged(object sender, EventArgs e)
        {
            offsetY = this.vsbPanel.Value * 1.0f;
            this.pnlCanvas.Invalidate();
        }


        private void hsbPanel_Scroll(object sender, ScrollEventArgs e)
        {
            offsetX = this.hsbPanel.Value * 1.0f;
            this.pnlCanvas.Invalidate();
        }

        private void txtWidth_Validating(object sender, CancelEventArgs e)
        {
            try
            {
                cellWidth = (float)int.Parse(this.txtWidth.Text);
            }
            catch (System.Exception ex)
            {
                e.Cancel = true;
            }
            
        }

        float offsetY = 0.0f;
        float realH = 0.0f;
        float offsetX = 0.0f;
        float realW = 0.0f;
        float cellWidth = 200.0f;



    }
}
