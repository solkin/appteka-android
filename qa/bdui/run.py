import argparse
import os
import shlex
import subprocess
import tempfile
import zipfile
from pathlib import Path

parser = argparse.ArgumentParser(description="Verify BDUI against real Android framework APIs")
parser.add_argument('--device', required=True, help='adb device serial')
parser.add_argument('--apk', required=True, type=Path, help='debug APK signed with the default debug key')
parser.add_argument('--url', required=True, help='device-reachable URL serving the selected schema')
parser.add_argument('--mode', choices=['flipper', 'menu'], default='flipper')
parser.add_argument('--sdk', type=Path, default=os.environ.get('ANDROID_HOME'))
parser.add_argument('--build-tools', default='36.0.0')
args = parser.parse_args()
if args.sdk is None or 'JAVA_HOME' not in os.environ:
    parser.error('Set ANDROID_HOME (or --sdk) and JAVA_HOME')

root = Path(__file__).resolve().parent
java = Path(os.environ['JAVA_HOME'])
tools = args.sdk / 'build-tools' / args.build_tools
android = next((p for p in [args.sdk / 'platforms/android-37/android.jar',
                            args.sdk / 'platforms/android-37.0/android.jar'] if p.exists()), None)
if android is None:
    parser.error('Install the Android 37 platform')
adb = [str(args.sdk / 'platform-tools/adb'), '-s', args.device]


def run(command):
    return subprocess.run([str(arg) for arg in command], check=True, timeout=180)


with tempfile.TemporaryDirectory(prefix='appteka-bdui-') as directory:
    work = Path(directory)
    classes = work / 'classes'
    dex = work / 'dex'
    classes.mkdir()
    dex.mkdir()
    run([java / 'bin/javac', '--release', '17', '-classpath', android,
         '-d', classes, root / 'RegressionInstrumentation.java'])
    run([tools / 'd8', '--min-api', '23', '--lib', android, '--output', dex,
         *classes.rglob('*.class')])
    unsigned = work / 'unsigned.apk'
    run([tools / 'aapt2', 'link', '-I', android, '--manifest', root / 'AndroidManifest.xml',
         '-o', unsigned])
    with zipfile.ZipFile(unsigned, 'a') as apk:
        apk.write(dex / 'classes.dex', 'classes.dex')
    signed = work / 'runner.apk'
    run([tools / 'apksigner', 'sign', '--ks', Path.home() / '.android/debug.keystore',
         '--ks-pass', 'pass:android', '--out', signed, unsigned])
    run([*adb, 'install', '-r', args.apk])
    run([*adb, 'install', '-r', signed])
    command = ['am', 'instrument', '-w', '-e', 'url', args.url, '-e', 'mode', args.mode,
               'store.appteka.qa.runner/.RegressionInstrumentation']
    result = subprocess.run([*adb, 'shell', shlex.join(command)], check=True,
                            capture_output=True, text=True, timeout=90)
    print(result.stdout)
    print(result.stderr, end='')
    if 'INSTRUMENTATION_RESULT: result=verified' not in result.stdout:
        raise SystemExit('Native BDUI verification failed')
