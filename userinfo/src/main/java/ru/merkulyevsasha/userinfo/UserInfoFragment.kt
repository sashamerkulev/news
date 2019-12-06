package ru.merkulyevsasha.userinfo

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_userinfo.imageViewAvatar
import kotlinx.android.synthetic.main.fragment_userinfo.layoutButtonCamera
import kotlinx.android.synthetic.main.fragment_userinfo.layoutButtonGallery
import kotlinx.android.synthetic.main.fragment_userinfo.layoutSources
import kotlinx.android.synthetic.main.fragment_userinfo.layoutSwitchTheme
import kotlinx.android.synthetic.main.fragment_userinfo.switchTheme
import kotlinx.android.synthetic.main.fragment_userinfo.toolbar
import kotlinx.android.synthetic.main.fragment_userinfo.userName
import ru.merkulyevsasha.core.RequireServiceLocator
import ru.merkulyevsasha.core.ServiceLocator
import ru.merkulyevsasha.core.models.ThemeEnum
import ru.merkulyevsasha.core.models.UserInfo
import ru.merkulyevsasha.core.models.UserProfile
import ru.merkulyevsasha.core.presentation.OnThemeChangedCallback
import ru.merkulyevsasha.coreandroid.common.AvatarShower
import ru.merkulyevsasha.coreandroid.common.ColorThemeResolver
import ru.merkulyevsasha.coreandroid.common.KbUtils
import ru.merkulyevsasha.coreandroid.common.ToolbarCombinator
import java.io.IOException

class UserInfoFragment : Fragment(), UserInfoView, RequireServiceLocator {

    companion object {
        @JvmStatic
        val TAG: String = UserInfoFragment::class.java.simpleName

        private const val KEY_FILE_NAME = "key_file_name"

        private const val GALLERY_TAKE_IMAGE_REQUEST = 1001
        private const val CAMERA_TAKE_IMAGE_REQUEST = 1003

        @JvmStatic
        fun newInstance(): Fragment {
            val fragment = UserInfoFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    private lateinit var serviceLocator: ServiceLocator
    private var presenter: UserInfoPresenterImpl? = null
    private var combinator: ToolbarCombinator? = null

    private lateinit var colorThemeResolver: ColorThemeResolver
    private val avatarShower = AvatarShower()

    private var profileFileName: String = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ToolbarCombinator) {
            combinator = context
        }
    }

    override fun setServiceLocator(serviceLocator: ServiceLocator) {
        this.serviceLocator = serviceLocator
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_userinfo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val savedState = savedInstanceState ?: arguments
        savedState?.apply {
            profileFileName = savedState.getString(KEY_FILE_NAME, "")
        }

        colorThemeResolver = ColorThemeResolver(TypedValue(), requireContext().theme)

        toolbar.setTitle(R.string.fragment_user_title)
        toolbar.setTitleTextColor(colorThemeResolver.getThemeAttrColor(R.attr.actionBarTextColor))
        combinator?.bindToolbar(toolbar)

        presenter = serviceLocator.get(UserInfoPresenterImpl::class.java)
        presenter?.bindView(this)
        presenter?.onFirstLoad()

        layoutButtonCamera.setOnClickListener { presenter?.onLoadCameraClicked() }
        layoutButtonGallery.setOnClickListener { presenter?.onLoadGalleryClick() }

        userName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                KbUtils.hideKeyboard(requireActivity())
                presenter?.onSaveButtonClicked(userName.text.toString())
            }
            true
        }

        layoutSwitchTheme.setOnClickListener {
            val activity = requireActivity()
            if (activity is OnThemeChangedCallback) {
                val isChecked = !switchTheme.isChecked
                val newTheme = if (isChecked) ThemeEnum.ClassicNight else ThemeEnum.Classic
                activity.onThemeChanged(newTheme)
                presenter?.onThemeChanged(newTheme)
            }
        }
    }

    override fun onPause() {
        presenter?.unbindView()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter?.bindView(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveFragmentState(outState)
    }

    override fun onDestroy() {
        combinator?.unbindToolbar()
        saveFragmentState(arguments ?: Bundle())
        serviceLocator.release(UserInfoPresenterImpl::class.java)
        presenter?.onDestroy()
        presenter = null
        super.onDestroy()
    }

    override fun showError() {
        Toast.makeText(requireContext(), getString(R.string.user_info_loading_error_message), Toast.LENGTH_LONG).show()
    }

    override fun showNameRequiredValidationMessage() {
        Toast.makeText(requireContext(), getString(R.string.user_info_name_require_error_message), Toast.LENGTH_LONG).show()
    }

    override fun showSaveError() {
        Toast.makeText(requireContext(), getString(R.string.user_info_uniq_name_error_message), Toast.LENGTH_LONG).show()
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun showUserInfo(userInfo: UserInfo) {
        if (userInfo.avatarUrl.isNotEmpty()) {
            avatarShower.showWithoutCache(requireContext(), R.drawable.ic_avatar_empty, userInfo.avatarUrl, userInfo.authorization, imageViewAvatar)
        }
        userName.setText(userInfo.name)
    }

    override fun showUserProfile(userProfile: UserProfile) {
        showUserInfo(userProfile.userInfo)
        switchTheme.isChecked = userProfile.theme == ThemeEnum.ClassicNight


        layoutSources.removeAllViews()
        userProfile.sources.forEach { source ->
            val view = CheckBox(requireContext())
            view.text = source.sourceName
            view.isChecked = source.checked
            view.setTextColor(colorThemeResolver.getThemeAttrColor(R.attr.activeColor))
            view.setOnClickListener {
                presenter?.onSourceChecked(view.isChecked, source.sourceId)
            }
            layoutSources.addView(view)
        }
    }

    override fun showSuccesSaving() {
        userName.clearFocus()
        Toast.makeText(requireContext(), getString(R.string.user_info_name_save_success_message), Toast.LENGTH_LONG).show()
    }

    override fun takeGalleryPicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.userinfo_select_image_label)), GALLERY_TAKE_IMAGE_REQUEST)
    }

    override fun takeCameraPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the File where the photo should go
            profileFileName = ru.merkulyevsasha.coreandroid.common.ImageFileHelper.getTempFileName()
            val helper = ru.merkulyevsasha.coreandroid.common.ImageFileHelper(requireContext(), profileFileName)
            try {
                helper.file().createNewFile()
                if (helper.file() != null) {
                    val photoURI = FileProvider.getUriForFile(requireActivity(), getString(R.string.APPLICATION_ID) + ".fileprovider", helper.file())
                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(helper.file()));
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_TAKE_IMAGE_REQUEST)
                }
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == CAMERA_TAKE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val helper = ru.merkulyevsasha.coreandroid.common.ImageFileHelper(requireContext(), profileFileName)
            helper.compress()
            profileFileName = helper.file().absolutePath
            Glide.with(this).load(helper.file()).into(imageViewAvatar)
            presenter?.onChangedAvatar(profileFileName)
        }
        if (requestCode == GALLERY_TAKE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            intent?.let {
                it.data?.let { uri ->
                    profileFileName = ru.merkulyevsasha.coreandroid.common.ImageFileHelper.getTempFileName()
                    val helper = ru.merkulyevsasha.coreandroid.common.ImageFileHelper(requireContext(), profileFileName)
                    helper.createImageFile(requireActivity().contentResolver.openInputStream(uri))
                    helper.compress()
                    profileFileName = helper.file().absolutePath
                    Glide.with(this).load(helper.file()).into(imageViewAvatar)
                    presenter?.onChangedAvatar(profileFileName)
                }
            }
        }
    }

    private fun saveFragmentState(state: Bundle) {
        state.putString(KEY_FILE_NAME, profileFileName)
    }

}