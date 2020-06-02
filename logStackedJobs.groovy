import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

//アラームパラメータ
def HOUR = 60 * 60  * 1000            // 1時間のミリ秒
def alarmValue = HOUR * 12          //  12時間のミリ秒
def warningMessage = " takes TOO LONG!"  // アラーム超過時の警告メッセージ

// しきい値超過時にジョブを停止させるオプション
def doStop = false 

// コンソール出力オブジェクト取得
def out = getBinding().getVariables().get("out")
// ファイル出力
def logFile = new FilePath(new File(build.workspace.toString() + "/alarm.log"))

// 実行中のビルドを取得
def runningBuilds = Jenkins.instance.getItems().collect { job->
    job.builds.findAll { it.getResult().equals(null) }
}.flatten()

// 実行中のビルドをコンソールログに出力
for (build in runningBuilds ) {
    // 実行中のジョブの時間を取得
    def executionTime =  System.currentTimeMillis() - build.getTimeInMillis()

    // コンソールに出力
    out.println(build.toString() + " takes " + executionTime/1000 + "sec.")

    // アラームしきい値設定を超過している場合はエラーメッセージを追加
    if (alarmValue < executionTime) {
        out.println(build.toString() + warningMessage)
        logFile.write(build.toString() + warningMessage)
    } 

   // ビルド停止オプションが有効な場合
   if (doStop) {
        out.println("try to stop " + build.toString())
        build.doStop()
   }    
}
