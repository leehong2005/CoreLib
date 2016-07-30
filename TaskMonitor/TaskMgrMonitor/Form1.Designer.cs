namespace TaskMgrMonitor
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.pnlCanvas = new System.Windows.Forms.Panel();
            this.btnMonitor = new System.Windows.Forms.Button();
            this.btnClear = new System.Windows.Forms.Button();
            this.btnRefresh = new System.Windows.Forms.Button();
            this.vsbPanel = new System.Windows.Forms.VScrollBar();
            this.hsbPanel = new System.Windows.Forms.HScrollBar();
            this.txtWidth = new System.Windows.Forms.TextBox();
            this.lblWidth = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // pnlCanvas
            // 
            this.pnlCanvas.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.pnlCanvas.Location = new System.Drawing.Point(0, 0);
            this.pnlCanvas.Name = "pnlCanvas";
            this.pnlCanvas.Size = new System.Drawing.Size(435, 408);
            this.pnlCanvas.TabIndex = 0;
            // 
            // btnMonitor
            // 
            this.btnMonitor.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnMonitor.Location = new System.Drawing.Point(461, 385);
            this.btnMonitor.Name = "btnMonitor";
            this.btnMonitor.Size = new System.Drawing.Size(75, 23);
            this.btnMonitor.TabIndex = 1;
            this.btnMonitor.Text = "Monitor";
            this.btnMonitor.UseVisualStyleBackColor = true;
            this.btnMonitor.Click += new System.EventHandler(this.btnMonitor_Click);
            // 
            // btnClear
            // 
            this.btnClear.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnClear.Location = new System.Drawing.Point(461, 415);
            this.btnClear.Name = "btnClear";
            this.btnClear.Size = new System.Drawing.Size(75, 23);
            this.btnClear.TabIndex = 2;
            this.btnClear.Text = "Clear";
            this.btnClear.UseVisualStyleBackColor = true;
            this.btnClear.Click += new System.EventHandler(this.btnClear_Click);
            // 
            // btnRefresh
            // 
            this.btnRefresh.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnRefresh.Location = new System.Drawing.Point(461, 355);
            this.btnRefresh.Name = "btnRefresh";
            this.btnRefresh.Size = new System.Drawing.Size(75, 24);
            this.btnRefresh.TabIndex = 3;
            this.btnRefresh.Text = "Refresh";
            this.btnRefresh.UseVisualStyleBackColor = true;
            this.btnRefresh.Click += new System.EventHandler(this.btnRefresh_Click);
            // 
            // vsbPanel
            // 
            this.vsbPanel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.vsbPanel.Enabled = false;
            this.vsbPanel.Location = new System.Drawing.Point(438, 0);
            this.vsbPanel.Name = "vsbPanel";
            this.vsbPanel.Size = new System.Drawing.Size(18, 408);
            this.vsbPanel.TabIndex = 4;
            this.vsbPanel.ValueChanged += new System.EventHandler(this.vsbPanel_ValueChanged);
            // 
            // hsbPanel
            // 
            this.hsbPanel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.hsbPanel.Enabled = false;
            this.hsbPanel.Location = new System.Drawing.Point(0, 412);
            this.hsbPanel.Name = "hsbPanel";
            this.hsbPanel.Size = new System.Drawing.Size(435, 17);
            this.hsbPanel.TabIndex = 5;
            this.hsbPanel.Scroll += new System.Windows.Forms.ScrollEventHandler(this.hsbPanel_Scroll);
            // 
            // txtWidth
            // 
            this.txtWidth.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.txtWidth.Location = new System.Drawing.Point(500, 6);
            this.txtWidth.Name = "txtWidth";
            this.txtWidth.Size = new System.Drawing.Size(36, 20);
            this.txtWidth.TabIndex = 6;
            this.txtWidth.Text = "300";
            this.txtWidth.Validating += new System.ComponentModel.CancelEventHandler(this.txtWidth_Validating);
            // 
            // lblWidth
            // 
            this.lblWidth.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.lblWidth.AutoSize = true;
            this.lblWidth.Location = new System.Drawing.Point(463, 9);
            this.lblWidth.Name = "lblWidth";
            this.lblWidth.Size = new System.Drawing.Size(35, 13);
            this.lblWidth.TabIndex = 7;
            this.lblWidth.Text = "Width";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(548, 444);
            this.Controls.Add(this.lblWidth);
            this.Controls.Add(this.txtWidth);
            this.Controls.Add(this.hsbPanel);
            this.Controls.Add(this.vsbPanel);
            this.Controls.Add(this.btnRefresh);
            this.Controls.Add(this.btnClear);
            this.Controls.Add(this.btnMonitor);
            this.Controls.Add(this.pnlCanvas);
            this.Name = "Form1";
            this.Text = "Task Monitor";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Panel pnlCanvas;
        private System.Windows.Forms.Button btnMonitor;
        private System.Windows.Forms.Button btnClear;
        private System.Windows.Forms.Button btnRefresh;
        private System.Windows.Forms.VScrollBar vsbPanel;
        private System.Windows.Forms.HScrollBar hsbPanel;
        private System.Windows.Forms.TextBox txtWidth;
        private System.Windows.Forms.Label lblWidth;

    }
}

