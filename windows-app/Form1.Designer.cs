namespace TGAutoResponderDesktop;

using System.Drawing;
using System.Windows.Forms;

partial class Form1
{
    private System.ComponentModel.IContainer components = null;

    private Label lblTitle;
    private Label lblSubTitle;
    private Label lblToken;
    private TextBox txtToken;
    private Label lblReply;
    private TextBox txtReply;
    private Label lblSticker;
    private TextBox txtSticker;
    private Label lblCooldown;
    private NumericUpDown numCooldown;
    private Label lblMinutes;
    private Button btnStart;
    private Button btnStop;
    private Label lblStatus;
    private Label lblLogs;
    private ListBox lstLogs;

    protected override void Dispose(bool disposing)
    {
        if (disposing && (components != null))
        {
            components.Dispose();
        }
        base.Dispose(disposing);
    }

    private void InitializeComponent()
    {
        this.lblTitle = new Label();
        this.lblSubTitle = new Label();
        this.lblToken = new Label();
        this.txtToken = new TextBox();
        this.lblReply = new Label();
        this.txtReply = new TextBox();
        this.lblSticker = new Label();
        this.txtSticker = new TextBox();
        this.lblCooldown = new Label();
        this.numCooldown = new NumericUpDown();
        this.lblMinutes = new Label();
        this.btnStart = new Button();
        this.btnStop = new Button();
        this.lblStatus = new Label();
        this.lblLogs = new Label();
        this.lstLogs = new ListBox();

        ((System.ComponentModel.ISupportInitialize)(this.numCooldown)).BeginInit();
        this.SuspendLayout();

        // Title
        this.lblTitle.Text = "✈ TG AutoResponder";
        this.lblTitle.Font = new Font("Segoe UI", 16f, FontStyle.Bold);
        this.lblTitle.ForeColor = Color.FromArgb(42, 171, 238);
        this.lblTitle.Location = new Point(20, 15);
        this.lblTitle.Size = new Size(350, 32);

        this.lblSubTitle.Text = "Автоответчик для Telegram (Текст + Любые стикеры)";
        this.lblSubTitle.Font = new Font("Segoe UI", 9f);
        this.lblSubTitle.ForeColor = Color.FromArgb(127, 145, 164);
        this.lblSubTitle.Location = new Point(22, 47);
        this.lblSubTitle.Size = new Size(450, 20);

        // Token
        this.lblToken.Text = "Токен бота (из @BotFather):";
        this.lblToken.Location = new Point(22, 75);
        this.lblToken.Size = new Size(200, 18);
        this.lblToken.ForeColor = Color.FromArgb(127, 145, 164);

        this.txtToken.Location = new Point(24, 95);
        this.txtToken.Size = new Size(520, 26);
        this.txtToken.BackColor = Color.FromArgb(23, 33, 43);
        this.txtToken.ForeColor = Color.White;
        this.txtToken.BorderStyle = BorderStyle.FixedSingle;

        // Reply text
        this.lblReply.Text = "Текст ответа (поддерживает любые эмодзи):";
        this.lblReply.Location = new Point(22, 130);
        this.lblReply.Size = new Size(320, 18);
        this.lblReply.ForeColor = Color.FromArgb(127, 145, 164);

        this.txtReply.Location = new Point(24, 150);
        this.txtReply.Size = new Size(520, 60);
        this.txtReply.Multiline = true;
        this.txtReply.BackColor = Color.FromArgb(23, 33, 43);
        this.txtReply.ForeColor = Color.White;
        this.txtReply.BorderStyle = BorderStyle.FixedSingle;
        this.txtReply.Text = "Привет! Сейчас я занят, отвечу немного позже 👋";

        // Sticker
        this.lblSticker.Text = "ID любого стикера Telegram (опционально):";
        this.lblSticker.Location = new Point(22, 220);
        this.lblSticker.Size = new Size(350, 18);
        this.lblSticker.ForeColor = Color.FromArgb(127, 145, 164);

        this.txtSticker.Location = new Point(24, 240);
        this.txtSticker.Size = new Size(520, 26);
        this.txtSticker.BackColor = Color.FromArgb(23, 33, 43);
        this.txtSticker.ForeColor = Color.White;
        this.txtSticker.BorderStyle = BorderStyle.FixedSingle;

        // Cooldown
        this.lblCooldown.Text = "Кулдаун (не повторять ответ чаще, чем раз в):";
        this.lblCooldown.Location = new Point(22, 275);
        this.lblCooldown.Size = new Size(330, 20);
        this.lblCooldown.ForeColor = Color.FromArgb(127, 145, 164);

        this.numCooldown.Location = new Point(360, 273);
        this.numCooldown.Size = new Size(60, 24);
        this.numCooldown.Minimum = 1;
        this.numCooldown.Maximum = 1440;
        this.numCooldown.Value = 10;
        this.numCooldown.BackColor = Color.FromArgb(23, 33, 43);
        this.numCooldown.ForeColor = Color.White;

        this.lblMinutes.Text = "минут";
        this.lblMinutes.Location = new Point(426, 275);
        this.lblMinutes.Size = new Size(60, 20);
        this.lblMinutes.ForeColor = Color.FromArgb(127, 145, 164);

        // Buttons
        this.btnStart.Text = "▶ Запустить";
        this.btnStart.Location = new Point(24, 315);
        this.btnStart.Size = new Size(160, 38);
        this.btnStart.BackColor = Color.FromArgb(42, 171, 238);
        this.btnStart.ForeColor = Color.White;
        this.btnStart.FlatStyle = FlatStyle.Flat;
        this.btnStart.FlatAppearance.BorderSize = 0;
        this.btnStart.Font = new Font("Segoe UI", 10f, FontStyle.Bold);
        this.btnStart.Cursor = Cursors.Hand;
        this.btnStart.Click += new System.EventHandler(this.BtnStart_Click);

        this.btnStop.Text = "⏹ Остановить";
        this.btnStop.Location = new Point(195, 315);
        this.btnStop.Size = new Size(160, 38);
        this.btnStop.BackColor = Color.FromArgb(36, 47, 61);
        this.btnStop.ForeColor = Color.White;
        this.btnStop.FlatStyle = FlatStyle.Flat;
        this.btnStop.FlatAppearance.BorderSize = 0;
        this.btnStop.Font = new Font("Segoe UI", 10f, FontStyle.Bold);
        this.btnStop.Enabled = false;
        this.btnStop.Cursor = Cursors.Hand;
        this.btnStop.Click += new System.EventHandler(this.BtnStop_Click);

        // Status
        this.lblStatus.Text = "⚪ Статус: Остановлен";
        this.lblStatus.Location = new Point(24, 365);
        this.lblStatus.Size = new Size(520, 22);
        this.lblStatus.Font = new Font("Segoe UI", 9.5f, FontStyle.Bold);
        this.lblStatus.ForeColor = Color.FromArgb(127, 145, 164);

        // Logs
        this.lblLogs.Text = "История ответов и событий:";
        this.lblLogs.Location = new Point(22, 395);
        this.lblLogs.Size = new Size(250, 18);
        this.lblLogs.ForeColor = Color.FromArgb(127, 145, 164);

        this.lstLogs.Location = new Point(24, 415);
        this.lstLogs.Size = new Size(520, 160);
        this.lstLogs.BackColor = Color.FromArgb(23, 33, 43);
        this.lstLogs.ForeColor = Color.FromArgb(200, 215, 230);
        this.lstLogs.BorderStyle = BorderStyle.FixedSingle;

        // Form
        this.ClientSize = new Size(568, 595);
        this.Controls.Add(this.lblTitle);
        this.Controls.Add(this.lblSubTitle);
        this.Controls.Add(this.lblToken);
        this.Controls.Add(this.txtToken);
        this.Controls.Add(this.lblReply);
        this.Controls.Add(this.txtReply);
        this.Controls.Add(this.lblSticker);
        this.Controls.Add(this.txtSticker);
        this.Controls.Add(this.lblCooldown);
        this.Controls.Add(this.numCooldown);
        this.Controls.Add(this.lblMinutes);
        this.Controls.Add(this.btnStart);
        this.Controls.Add(this.btnStop);
        this.Controls.Add(this.lblStatus);
        this.Controls.Add(this.lblLogs);
        this.Controls.Add(this.lstLogs);

        ((System.ComponentModel.ISupportInitialize)(this.numCooldown)).EndInit();
        this.ResumeLayout(false);
        this.PerformLayout();
    }
}
