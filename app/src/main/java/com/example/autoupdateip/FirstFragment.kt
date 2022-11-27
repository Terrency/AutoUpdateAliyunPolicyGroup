package com.example.autoupdateip

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aliyun.ecs20140526.Client
import com.aliyun.ecs20140526.models.AuthorizeSecurityGroupRequest
import com.aliyun.ecs20140526.models.RevokeSecurityGroupRequest
import com.aliyun.tea.TeaException
import com.aliyun.teaopenapi.models.Config
import com.aliyun.teautil.Common
import com.aliyun.teautil.models.RuntimeOptions
import com.example.autoupdateip.databinding.FragmentFirstBinding
import okhttp3.*
import java.io.IOException


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var ip: String = ""
    private val config = Config().setAccessKeyId("").setAccessKeySecret("").setEndpoint("ecs.cn-hongkong.aliyuncs.com")
    private val client = Client(config)
    private var sharedPreferences: SharedPreferences? = null;
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.buttonMobile.setOnClickListener{
            val oldIp = sharedPreferences?.getString("mobileName","");
            if(oldIp != null && oldIp.isNotEmpty()){
                val oldMobile = Policy("10", oldIp, "手机")
                println(oldMobile)
                deleteAliyunSecurityPolicyRule(oldMobile)
            }
            getOwnIP(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                }
                override fun onResponse(call: Call?, response: Response?) {
                    if(response == null) throw IOException("Unexpected code $response")
                    val ip = response.body()!!.string().trim()
                    println(ip)
                    val mobile = Policy("10", ip, "手机")
                    addAliyunSecurityPolicyRule(mobile)
                    val editor = sharedPreferences?.edit()
                    //步骤3：将获取过来的值放入文件
                    editor?.putString("mobileName", ip)
                    editor?.commit()
                }
            })
        }
        binding.buttonHome.setOnClickListener{
            val oldIp = sharedPreferences?.getString("homeName","");
            if(oldIp!= null && oldIp.isNotEmpty()){
                val oldMobile = Policy("20", oldIp, "家里")
                println(oldMobile)
                deleteAliyunSecurityPolicyRule(oldMobile)
            }
            getOwnIP(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                }
                override fun onResponse(call: Call?, response: Response?) {
                    if(response == null) throw IOException("Unexpected code $response")
                    val ip = response.body()!!.string().trim()
                    println(ip)
                    val home = Policy("20", ip, "家里")
                    addAliyunSecurityPolicyRule(home)
                    val editor = sharedPreferences?.edit()
                    //步骤3：将获取过来的值放入文件
                    editor?.putString("homeName", ip)
                    editor?.commit()
                }
            })
        }
    }
    fun getOwnIP(callback: Callback){
        val url = "https://icanhazip.com/"
        val okHttp = OkHttpClient()
        val request = Request.Builder().url(url).get().build();
        okHttp.newCall(request).enqueue(callback)
    }
    data class Policy(val priority: String, val ip: String, val desc: String){}
    fun addAliyunSecurityPolicyRule(policy: Policy){
        val permissions0 = AuthorizeSecurityGroupRequest.AuthorizeSecurityGroupRequestPermissions()
            .setPolicy("accept")
            .setPriority(policy.priority)
            .setIpProtocol("tcp")
            .setSourceCidrIp(policy.ip)
            .setPortRange("8001/8001")
            .setDescription(policy.desc)
        val authorizeSecurityGroupRequest = AuthorizeSecurityGroupRequest()
            .setRegionId("cn-hongkong")
            .setSecurityGroupId("sg-j6c7d6wqj0f2917vt6rt")
            .setPermissions(java.util.Arrays.asList(
                permissions0
            ))
        val runtime = RuntimeOptions()
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.authorizeSecurityGroupWithOptions(authorizeSecurityGroupRequest, runtime)
        } catch (error: TeaException) {
            // 如有需要，请打印 error
            Common.assertAsString(error.message)
        } catch (_error: Exception) {
            val error = TeaException(_error.message, _error)
            // 如有需要，请打印 error
            Common.assertAsString(error.message)
        }
    }
    fun deleteAliyunSecurityPolicyRule(policy: Policy){
        val permissions0 = RevokeSecurityGroupRequest.RevokeSecurityGroupRequestPermissions()
            .setPolicy("accept")
            .setPriority(policy.priority)
            .setIpProtocol("tcp")
            .setSourceCidrIp(policy.ip)
            .setPortRange("8001/8001")
            .setDescription(policy.desc)
        val revokeSecurityGroupRequest = RevokeSecurityGroupRequest()
            .setRegionId("cn-hongkong")
            .setSecurityGroupId("sg-j6c7d6wqj0f2917vt6rt")
            .setPermissions(listOf(
                permissions0
            ))
        val runtime = RuntimeOptions()
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.revokeSecurityGroupWithOptions(revokeSecurityGroupRequest, runtime)
        } catch (error: TeaException) {
            // 如有需要，请打印 error
            Common.assertAsString(error.message)
        } catch (_error: Exception) {
            val error = TeaException(_error.message, _error)
            // 如有需要，请打印 error
            Common.assertAsString(error.message)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}