using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;

namespace TestMonitor
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            UdpClient udpClient = new UdpClient("192.168.7.91", 1113);
            Byte[] sendBytes = Encoding.ASCII.GetBytes(this.textBox1.Text);
            try
            {
                udpClient.Send(sendBytes, sendBytes.Length);
            }
            catch (Exception ex)
            {
                Console.WriteLine(e.ToString());
            }

        }
    }
}
