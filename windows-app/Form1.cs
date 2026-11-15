namespace TGAutoResponderDesktop;

using System;
using System.Drawing;
using System.IO;
using System.Net.Http;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Collections.Concurrent;

public partial class Form1 : Form
{
    private static readonly HttpClient httpClient = new HttpClient();
    private CancellationTokenSource? cts;
    private long lastUpdateId = 0;
    private readonly ConcurrentDictionary<long, DateTime> lastReplyTimes = new();

    public Form1()
    {
        InitializeComponent();
        ApplyDarkTheme();
    }

    private void ApplyDarkTheme()
    {
        this.BackColor = Color.FromArgb(14, 22, 33);
        this.ForeColor = Color.White;
        this.Font = new Font("Segoe UI", 9.5f, FontStyle.Regular);
        this.FormBorderStyle = FormBorderStyle.FixedSingle;
        this.MaximizeBox = false;
        this.Text = "TG AutoResponder - Windows Edition";
    }

    private void BtnStart_Click(object sender, EventArgs e)
    {
        string token = txtToken.Text.Trim();
        string replyText = txtReply.Text.Trim();
        string stickerId = txtSticker.Text.Trim();

        if (string.IsNullOrEmpty(token))
        {
            MessageBox.Show("Пожалуйста, введите токен бота из @BotFather!", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            return;
        }

        if (string.IsNullOrEmpty(replyText) && string.IsNullOrEmpty(stickerId))
        {
            MessageBox.Show("Введите текст ответа или ID стикера!", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            return;
        }

        cts = new CancellationTokenSource();
        btnStart.Enabled = false;
        btnStop.Enabled = true;
        txtToken.Enabled = false;
        lblStatus.Text = "🟢 Статус: Активен (слушает сообщения)";
        lblStatus.ForeColor = Color.FromArgb(79, 174, 78);

        Log("Служба автоответчика запущена!");

        Task.Run(() => PollingLoop(token, replyText, stickerId, (int)numCooldown.Value, cts.Token));
    }

    private void BtnStop_Click(object sender, EventArgs e)
    {
        cts?.Cancel();
        btnStart.Enabled = true;
        btnStop.Enabled = false;
        txtToken.Enabled = true;
        lblStatus.Text = "🔴 Статус: Остановлен";
        lblStatus.ForeColor = Color.FromArgb(229, 57, 53);
        Log("Служба автоответчика остановлена.");
    }

    private async Task PollingLoop(string token, string replyText, string stickerId, int cooldownMinutes, CancellationToken tokenCt)
    {
        while (!tokenCt.IsCancellationRequested)
        {
            try
            {
                string url = $"https://api.telegram.org/bot{token}/getUpdates?offset={lastUpdateId + 1}&timeout=20";
                var response = await httpClient.GetStringAsync(url, tokenCt);

                using var doc = JsonDocument.Parse(response);
                var root = doc.RootElement;

                if (root.GetProperty("ok").GetBoolean())
                {
                    var result = root.GetProperty("result");
                    foreach (var update in result.EnumerateArray())
                    {
                        long updateId = update.GetProperty("update_id").GetInt64();
                        lastUpdateId = Math.Max(lastUpdateId, updateId);

                        if (update.TryGetProperty("message", out var message))
                        {
                            await HandleIncomingMessage(token, message, replyText, stickerId, cooldownMinutes, tokenCt);
                        }
                    }
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                Invoke(() => Log($"⚠️ Ошибка: {ex.Message}"));
                await Task.Delay(3000, tokenCt);
            }
        }
    }

    private async Task HandleIncomingMessage(string token, JsonElement message, string replyText, string stickerId, int cooldownMinutes, CancellationToken ct)
    {
        var chat = message.GetProperty("chat");
        long chatId = chat.GetProperty("id").GetInt64();
        string chatType = chat.TryGetProperty("type", out var t) ? t.GetString() ?? "" : "";
        string fromName = chat.TryGetProperty("first_name", out var fn) ? fn.GetString() ?? "Пользователь" : "Пользователь";

        if (chatType != "private") return;

        DateTime now = DateTime.UtcNow;
        if (lastReplyTimes.TryGetValue(chatId, out DateTime lastTime))
        {
            if ((now - lastTime).TotalMinutes < cooldownMinutes)
            {
                return;
            }
        }

        lastReplyTimes[chatId] = now;

        Invoke(() => Log($"📩 Сообщение от {fromName} (ID: {chatId}). Отправка..."));

        if (!string.IsNullOrEmpty(replyText))
        {
            string sendTextUrl = $"https://api.telegram.org/bot{token}/sendMessage";
            var textPayload = new
            {
                chat_id = chatId,
                text = replyText
            };
            var content = new StringContent(JsonSerializer.Serialize(textPayload), System.Text.Encoding.UTF8, "application/json");
            await httpClient.PostAsync(sendTextUrl, content, ct);
        }

        if (!string.IsNullOrEmpty(stickerId))
        {
            string sendStickerUrl = $"https://api.telegram.org/bot{token}/sendSticker";
            var stickerPayload = new
            {
                chat_id = chatId,
                sticker = stickerId
            };
            var content = new StringContent(JsonSerializer.Serialize(stickerPayload), System.Text.Encoding.UTF8, "application/json");
            await httpClient.PostAsync(sendStickerUrl, content, ct);
        }

        Invoke(() => Log($"✅ Ответ успешно отправлен для {fromName}!"));
    }

    private void Log(string message)
    {
        string timestamp = DateTime.Now.ToString("HH:mm:ss");
        lstLogs.Items.Insert(0, $"[{timestamp}] {message}");
        if (lstLogs.Items.Count > 100) lstLogs.Items.RemoveAt(lstLogs.Items.Count - 1);
    }
}
