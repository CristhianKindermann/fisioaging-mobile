import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from scipy import signal

# Configurar o estilo dos gráficos
plt.style.use("seaborn-v0_8")
sns.set_palette("husl")


def load_sensor_data(csv_file_path):
    """
    Carrega os dados do CSV dos sensores
    """
    try:
        df = pd.read_csv(csv_file_path)

        # Converter timestamp para datetime
        df["datetime"] = pd.to_datetime(df["timestamp"], unit="ms")

        # Converter tempo decorrido para segundos
        df["elapsed_seconds"] = df["elapsed_ms"] / 1000.0

        print(f"📊 Dados carregados: {len(df)} pontos")
        print(f"⏱️ Duração: {df['elapsed_seconds'].max():.1f} segundos")
        print(f"📈 Frequência média: {len(df) / df['elapsed_seconds'].max():.1f} Hz")

        return df

    except Exception as e:
        print(f"❌ Erro ao carregar arquivo: {e}")
        return None


def plot_time_series(df):
    """
    Gráfico das séries temporais dos sensores
    """
    fig, axes = plt.subplots(2, 2, figsize=(15, 10))
    fig.suptitle(
        "📱 Dados dos Sensores ao Longo do Tempo", fontsize=16, fontweight="bold"
    )

    time = df["elapsed_seconds"]

    # Acelerômetro - componentes individuais
    axes[0, 0].plot(time, df["acc_x"], label="X", alpha=0.7)
    axes[0, 0].plot(time, df["acc_y"], label="Y", alpha=0.7)
    axes[0, 0].plot(time, df["acc_z"], label="Z", alpha=0.7)
    axes[0, 0].set_title("🏃 Acelerômetro (m/s²)")
    axes[0, 0].set_xlabel("Tempo (s)")
    axes[0, 0].set_ylabel("Aceleração (m/s²)")
    axes[0, 0].legend()
    axes[0, 0].grid(True, alpha=0.3)

    # Acelerômetro - magnitude
    axes[0, 1].plot(time, df["acc_magnitude"], color="red", linewidth=1.5)
    axes[0, 1].set_title("🏃 Magnitude da Aceleração")
    axes[0, 1].set_xlabel("Tempo (s)")
    axes[0, 1].set_ylabel("Magnitude (m/s²)")
    axes[0, 1].grid(True, alpha=0.3)

    # Giroscópio - componentes individuais
    axes[1, 0].plot(time, df["gyro_x"], label="X", alpha=0.7)
    axes[1, 0].plot(time, df["gyro_y"], label="Y", alpha=0.7)
    axes[1, 0].plot(time, df["gyro_z"], label="Z", alpha=0.7)
    axes[1, 0].set_title("🔄 Giroscópio (rad/s)")
    axes[1, 0].set_xlabel("Tempo (s)")
    axes[1, 0].set_ylabel("Velocidade Angular (rad/s)")
    axes[1, 0].legend()
    axes[1, 0].grid(True, alpha=0.3)

    # Giroscópio - magnitude
    axes[1, 1].plot(time, df["gyro_magnitude"], color="blue", linewidth=1.5)
    axes[1, 1].set_title("🔄 Magnitude da Rotação")
    axes[1, 1].set_xlabel("Tempo (s)")
    axes[1, 1].set_ylabel("Magnitude (rad/s)")
    axes[1, 1].grid(True, alpha=0.3)

    plt.tight_layout()
    plt.show()


def plot_3d_trajectory(df):
    """
    Gráfico 3D da trajetória no espaço
    """
    fig = plt.figure(figsize=(12, 5))

    # Acelerômetro 3D
    ax1 = fig.add_subplot(121, projection="3d")
    scatter = ax1.scatter(
        df["acc_x"],
        df["acc_y"],
        df["acc_z"],
        c=df["elapsed_seconds"],
        cmap="viridis",
        s=10,
        alpha=0.6,
    )
    ax1.set_xlabel("Aceleração X (m/s²)")
    ax1.set_ylabel("Aceleração Y (m/s²)")
    ax1.set_zlabel("Aceleração Z (m/s²)")
    ax1.set_title("🏃 Trajetória 3D - Acelerômetro")
    plt.colorbar(scatter, ax=ax1, shrink=0.5, label="Tempo (s)")

    # Giroscópio 3D
    ax2 = fig.add_subplot(122, projection="3d")
    scatter2 = ax2.scatter(
        df["gyro_x"],
        df["gyro_y"],
        df["gyro_z"],
        c=df["elapsed_seconds"],
        cmap="plasma",
        s=10,
        alpha=0.6,
    )
    ax2.set_xlabel("Rotação X (rad/s)")
    ax2.set_ylabel("Rotação Y (rad/s)")
    ax2.set_zlabel("Rotação Z (rad/s)")
    ax2.set_title("🔄 Trajetória 3D - Giroscópio")
    plt.colorbar(scatter2, ax=ax2, shrink=0.5, label="Tempo (s)")

    plt.tight_layout()
    plt.show()


def plot_frequency_analysis(df):
    """
    Análise de frequência (FFT) dos dados
    """
    fig, axes = plt.subplots(2, 2, figsize=(15, 10))
    fig.suptitle("🔊 Análise de Frequência (FFT)", fontsize=16, fontweight="bold")

    # Calcular frequência de amostragem
    dt = np.mean(np.diff(df["elapsed_seconds"]))
    fs = 1 / dt

    # FFT do acelerômetro
    fft_acc_mag = np.fft.fft(df["acc_magnitude"])
    freqs_acc = np.fft.fftfreq(len(fft_acc_mag), dt)

    # Plotar apenas frequências positivas
    positive_freqs = freqs_acc[: len(freqs_acc) // 2]
    positive_fft_acc = np.abs(fft_acc_mag[: len(fft_acc_mag) // 2])

    axes[0, 0].plot(positive_freqs, positive_fft_acc)
    axes[0, 0].set_title("FFT - Magnitude do Acelerômetro")
    axes[0, 0].set_xlabel("Frequência (Hz)")
    axes[0, 0].set_ylabel("Amplitude")
    axes[0, 0].grid(True, alpha=0.3)

    # Espectrograma do acelerômetro
    f, t, Sxx = signal.spectrogram(df["acc_magnitude"], fs=fs)
    im1 = axes[0, 1].pcolormesh(t, f, 10 * np.log10(Sxx), shading="gouraud")
    axes[0, 1].set_title("Espectrograma - Acelerômetro")
    axes[0, 1].set_xlabel("Tempo (s)")
    axes[0, 1].set_ylabel("Frequência (Hz)")
    plt.colorbar(im1, ax=axes[0, 1], label="Potência (dB)")

    # FFT do giroscópio
    fft_gyro_mag = np.fft.fft(df["gyro_magnitude"])
    positive_fft_gyro = np.abs(fft_gyro_mag[: len(fft_gyro_mag) // 2])

    axes[1, 0].plot(positive_freqs, positive_fft_gyro)
    axes[1, 0].set_title("FFT - Magnitude do Giroscópio")
    axes[1, 0].set_xlabel("Frequência (Hz)")
    axes[1, 0].set_ylabel("Amplitude")
    axes[1, 0].grid(True, alpha=0.3)

    # Espectrograma do giroscópio
    f2, t2, Sxx2 = signal.spectrogram(df["gyro_magnitude"], fs=fs)
    im2 = axes[1, 1].pcolormesh(t2, f2, 10 * np.log10(Sxx2), shading="gouraud")
    axes[1, 1].set_title("Espectrograma - Giroscópio")
    axes[1, 1].set_xlabel("Tempo (s)")
    axes[1, 1].set_ylabel("Frequência (Hz)")
    plt.colorbar(im2, ax=axes[1, 1], label="Potência (dB)")

    plt.tight_layout()
    plt.show()


def plot_statistical_analysis(df):
    """
    Análise estatística e histogramas
    """
    fig, axes = plt.subplots(2, 3, figsize=(18, 10))
    fig.suptitle("📊 Análise Estatística dos Sensores", fontsize=16, fontweight="bold")

    # Histogramas do acelerômetro
    axes[0, 0].hist(df["acc_x"], bins=50, alpha=0.7, color="red", label="X")
    axes[0, 0].hist(df["acc_y"], bins=50, alpha=0.7, color="green", label="Y")
    axes[0, 0].hist(df["acc_z"], bins=50, alpha=0.7, color="blue", label="Z")
    axes[0, 0].set_title("Histograma - Acelerômetro")
    axes[0, 0].set_xlabel("Aceleração (m/s²)")
    axes[0, 0].set_ylabel("Frequência")
    axes[0, 0].legend()
    axes[0, 0].grid(True, alpha=0.3)

    # Boxplot do acelerômetro
    acc_data = [df["acc_x"], df["acc_y"], df["acc_z"]]
    axes[0, 1].boxplot(acc_data, labels=["X", "Y", "Z"])
    axes[0, 1].set_title("Boxplot - Acelerômetro")
    axes[0, 1].set_ylabel("Aceleração (m/s²)")
    axes[0, 1].grid(True, alpha=0.3)

    # Correlação entre componentes do acelerômetro
    acc_corr = df[["acc_x", "acc_y", "acc_z"]].corr()
    im1 = axes[0, 2].imshow(acc_corr, cmap="coolwarm", aspect="auto", vmin=-1, vmax=1)
    axes[0, 2].set_title("Correlação - Acelerômetro")
    axes[0, 2].set_xticks(range(3))
    axes[0, 2].set_yticks(range(3))
    axes[0, 2].set_xticklabels(["X", "Y", "Z"])
    axes[0, 2].set_yticklabels(["X", "Y", "Z"])
    plt.colorbar(im1, ax=axes[0, 2])

    # Histogramas do giroscópio
    axes[1, 0].hist(df["gyro_x"], bins=50, alpha=0.7, color="red", label="X")
    axes[1, 0].hist(df["gyro_y"], bins=50, alpha=0.7, color="green", label="Y")
    axes[1, 0].hist(df["gyro_z"], bins=50, alpha=0.7, color="blue", label="Z")
    axes[1, 0].set_title("Histograma - Giroscópio")
    axes[1, 0].set_xlabel("Velocidade Angular (rad/s)")
    axes[1, 0].set_ylabel("Frequência")
    axes[1, 0].legend()
    axes[1, 0].grid(True, alpha=0.3)

    # Boxplot do giroscópio
    gyro_data = [df["gyro_x"], df["gyro_y"], df["gyro_z"]]
    axes[1, 1].boxplot(gyro_data, labels=["X", "Y", "Z"])
    axes[1, 1].set_title("Boxplot - Giroscópio")
    axes[1, 1].set_ylabel("Velocidade Angular (rad/s)")
    axes[1, 1].grid(True, alpha=0.3)

    # Correlação entre componentes do giroscópio
    gyro_corr = df[["gyro_x", "gyro_y", "gyro_z"]].corr()
    im2 = axes[1, 2].imshow(gyro_corr, cmap="coolwarm", aspect="auto", vmin=-1, vmax=1)
    axes[1, 2].set_title("Correlação - Giroscópio")
    axes[1, 2].set_xticks(range(3))
    axes[1, 2].set_yticks(range(3))
    axes[1, 2].set_xticklabels(["X", "Y", "Z"])
    axes[1, 2].set_yticklabels(["X", "Y", "Z"])
    plt.colorbar(im2, ax=axes[1, 2])

    plt.tight_layout()
    plt.show()


def print_statistics(df):
    """
    Imprimir estatísticas descritivas
    """
    print("\n" + "=" * 60)
    print("📈 ESTATÍSTICAS DESCRITIVAS DOS SENSORES")
    print("=" * 60)

    # Estatísticas do acelerômetro
    print("\n🏃 ACELERÔMETRO:")
    print("-" * 30)
    acc_stats = df[["acc_x", "acc_y", "acc_z", "acc_magnitude"]].describe()
    print(acc_stats.round(3))

    # Estatísticas do giroscópio
    print("\n🔄 GIROSCÓPIO:")
    print("-" * 30)
    gyro_stats = df[["gyro_x", "gyro_y", "gyro_z", "gyro_magnitude"]].describe()
    print(gyro_stats.round(3))

    # Informações gerais
    print(f"\n📊 INFORMAÇÕES GERAIS:")
    print(f"   • Total de pontos: {len(df):,}")
    print(f"   • Duração: {df['elapsed_seconds'].max():.2f} segundos")
    print(f"   • Frequência média: {len(df) / df['elapsed_seconds'].max():.1f} Hz")
    print(f"   • Aceleração máxima: {df['acc_magnitude'].max():.2f} m/s²")
    print(f"   • Rotação máxima: {df['gyro_magnitude'].max():.2f} rad/s")


def main():
    """
    Função principal para análise completa
    """
    # ALTERE ESTE CAMINHO para o seu arquivo CSV
    csv_file = "data2.csv"  # ← Coloque o nome do seu arquivo aqui

    print("🚀 Iniciando análise dos dados dos sensores...")

    # Carregar dados
    df = load_sensor_data(csv_file)

    if df is None:
        print("❌ Falha ao carregar os dados. Verifique o caminho do arquivo.")
        return

    # Estatísticas descritivas
    print_statistics(df)

    # Gerar todos os gráficos
    print("\n📊 Gerando gráficos...")

    plot_time_series(df)
    plot_3d_trajectory(df)
    plot_frequency_analysis(df)
    plot_statistical_analysis(df)

    print("\n✅ Análise completa! Todos os gráficos foram gerados.")


if __name__ == "__main__":
    main()
