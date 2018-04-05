package loCommonDialogs;

import javax.xml.bind.DatatypeConverter;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XStyleSettings;
import com.sun.star.awt.XStyleSettingsSupplier;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.graphic.XGraphic;
import com.sun.star.report.XFixedLine;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import loCommonDialogs.loChoiceBox.ActionListenerImpl;

public class loCustomMessageBox extends loDialogBox implements AutoCloseable {
	
	static int iconMessage	= 0;
	static int iconWarning	= 1;
	static int iconUsrError	= 2;
	static int iconSysError = 3;
	
	// Dialog and Control Size & Position Values
	private int labelvertpos, labelhorizpos;
	private int btnvertpos, okbtnhpos, cancelbtnhpos;
	private int lineVpos, lineHpos, linelength;
	private int vmargin;
	private int gap;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XFixedText	guiLabel2;
	private XButton		guiOKBtn;
	private XButton		guiCancelBtn;
	private XControl	guiIcon;
	
	public loCustomMessageBox(XComponentContext xComponentContext) {
		super(xComponentContext);
		gap				= margin;
		vmargin			= margin;		// Amount to offset everything from the top
		iconsize		= 32;
		dialogwidth		= 250;
		dialogheight	= 60;
		labelwidth		= dialogwidth - iconsize - (2*margin) - gap;
		labelheight		= iconsize - gap - 3;
		labelvertpos	= vmargin + (gap/2);
		labelhorizpos	= margin + iconsize + gap/2;
		btnvertpos		= dialogheight - btnheight - margin;
		okbtnhpos		= dialogwidth/2 - btnwidth - gap/2;
		//okbtnhpos		= dialogwidth - btnwidth - gap/2 - btnwidth - margin;
		cancelbtnhpos	= dialogwidth/2 + gap/2;
		//cancelbtnhpos	= dialogwidth - btnwidth - margin;
		lineVpos		= labelvertpos + labelheight; //vmargin + iconsize + vmargin;
		lineHpos		= labelhorizpos; //margin;
		linelength		= labelwidth; //dialogwidth - (2*margin);
		initBox();
	}
	
	// loDialogBox Abstract Method Definition
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);

		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Sizeable", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, true, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			// Message Icon (Default)
			String rawhexMessage = "89504E470D0A1A0A0000000D49484452000000300000003008060000005702F9870000000473424954080808087C086488000002A8494441546881EDD94F6B5C551880F1DFDC49860859D4D0C6148310081A86E2C2C5801010C4753651D48FE0C2ADA02EAA6BB7623E81B445B32915ECA25D9985A155D03A4C754840224D6A9A862EC4DEB9BDE3A21D1D133B7F6E66EE89749EE599C399E73DE77DEF3DF79C8283144C29299BB36C5EC58C59E34A87FAE541436CDB96BA759B56C5AA9AF6FEADDBCEB89794BD63D19253A67395EDC6EF6EFBC645552B1ABE6B35FF134051D91B2E98B760CC5810C96E241275355F78D3035528E2E1CCBFEE730BCE884441253B11899C34ED19AFB8E95BA95B450553CE78DFCB5E3BD6F2ED9C30E5AE71B7AD454ACA162D1DDBB4F92FC68C59B4A4A41C99B37CEC0AB6174E9936673932AF12DA2533F32A9119B3A13D323363360AF6921A04E34AFF8FA74E07460184A6E7677FF36C73981E87287C5CE8DEC993B402BDCE48DE3C392B30AA812131AA81D08C6A2034A31A08CD2880D04412496889CC2492C88EADD01E99D9B115A95B0FED9199BAF5C8865577EC8676E99B3B766D588DDC57B5E6D2506BA197B153695FE3ADB9E4BE6AA469CF0D2B36D58FE2D8916EC796A9B4AF73D94D7537AC68DA2DA2E981DFD4AC79D6AB4E785A417EAFDD7EE453A90DBFB8E02D0DD7691DAF3FFC715BCD35F73C65DA73264C0CC7F88050AFF2FBF65DB1EAAA0F345C6B351F9CE948C194092F7ADEDB5E50713AC315D3A4C9CCF2A9542C964834C46ED972D3BA9F9DF3A71F1E5D31FD5D2FC349958F74DE7B3F4E3E91F8D2A76ACEE25E2F7F95FF9DC0E3E463B1CBCEF7234FDE7BA14E337FD979DFFB501FF2E4BD02034A9B76C25E2B654C9B76C26DA78F9036ED84598123A64D3BF9073080B46927DF141A50DAB4339C00766C1F6A6BA5CD75EF4A07F71155ECDE2503FB4E5A50517C347E2CF6B5737EF49E01CD7C8BE10470D74F7EF58749A7EDD9F595CFD47CC2E03F9CFE028BEBE241CF81425C0000000049454E44AE426082";
			byte[] hexbinaryMessage = DatatypeConverter.parseHexBinary(rawhexMessage);
			guiIcon = insertImage(margin, vmargin, iconsize, iconsize, hexbinaryMessage, "png");
			
			// Warning Icon (Default)
			//String rawhexWarning = "89504E470D0A1A0A0000000D49484452000000300000003008060000005702F98700000006624B474400FF00FF00FFA0BDA793000000097048597300000B1300000B1301009A9C180000000774494D4507E2040210253784B9A8FA000003504944415468DEED583D6B1B41149C5984255418638C09214508526B42FA5B52A74A9D5F1852A6722D999426FD2D2684608231C6B8087721F8A5D0DE69F7BCF7A1FB100AD1C25923A3BB7B1FF366DF5B60BFF66BA797D86BB0C5818D1FFC5D6A888726B10600A446DF24717457E2D0EE3A30992FACB1724AF23835D1E5506956433D3889A3DF006DD879F62F39200040E200908CF8A3A168A486303E35D1939A4D8DC6104EA8DE8D8FF5751EF79C42B9133FFB76A2D70CA4467F02E5199D2D80BE7FA7691C7DDE590A89C87B800A229642CC8D17E1EA7DC4BB5D74C0528207B0152C923B05802073DC6B41777620892364854BA76E334C0B4424C7D93D7D38A13A721E93F9529E98221248103D9CC4D1F73E9C602FCA63F41F00239175E433EC46BE801F017C9CCC971FBAD8A17A509EBB6CA36A4A218B15D0BDA0473D749BC75EC8575F6EC7B3E549C1D11B00A72E9D481C0538B6150A65D4418836E3D9A2B4E04319717ECFAD512835FAB28C36A5D12AA1536AA2AFDBAC818C3A679EDA886C70BBAF4822386BAB48AD3290C4FA078091BB6179B87CA72E6E6A59D65462F4F53632605B657911A24D5B0A910405CFDB64416D6A7C12AF0A57449C4F1FD72A87E3698ED9AEE5569B156E7441D888D9663984EB0F29CAAF24D65F86C880D83F6F24FFC7BAD72FE24AD516AC3BD52096D79B6441352FDCE88AE074A51AE228480857F2A712939CA6467FEB2D03D91109C157A8A04D5F14B20178D9340BB50EE44724F49F381C8508AC36B75F7DF4425218C81DAA54E1F61472F0B4499FA4EA6533BAF763B80D0A594532FABE8E4AAAA6DFB9207914EA2386A550AE4847A9A99655964D5AE3D9427CEAB82D8F38B75763B2FB99EE78B6406A34435D2E9B731FC5516BCDD730FE05E04A206AA5603269EFC0B2D45E6E64BCD3BB64990861521EC6B3E56121AB0F000E830344035C3633B0C4F85B00276107EA2934992F4B0F013A0C80EE94C7D2224EE3E8BCCCF86DAB907FF12435FABC568584D44DA699EDA8908B01006FAB1CC87E3A959A31BAAE174A4CF418A0CFE3861B5968C89814F7057A9B96D160DDE6D75C850279EB67B9AAA49C08992687154D2824C531B9356DC2383591717BA1EC3533343AA469D20BF9B8136D0258D6B68AB2D1BF77B3CC8A943751215F91A4F78B99CD0521DFAFFDFA1FD75F34D243EF4CDF77560000000049454E44AE426082";
			//byte[] hexbinaryWarning = DatatypeConverter.parseHexBinary(rawhexWarning);
			//guiIcon = insertImage(margin, vmargin, iconsize, iconsize, hexbinaryWarning, "png");
			
			guiLabel 	 = insertFixedText(textalign_center, labelhorizpos, labelvertpos, labelwidth, labelheight, 0, "");
			guiLabel2	 = insertFixedText(textalign_center, margin, labelvertpos + labelheight - gap/2 - gap/4, dialogwidth, labelheight/2, 0, "");
			guiOKBtn 	 = insertButton(okbtnhpos, btnvertpos, btnwidth, btnheight, "OK", (short) PushButtonType.OK_value, true);
			guiCancelBtn = insertButton(cancelbtnhpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, true);
			
			//XControl guiLine = (XControl) insertFixedLine(lineHpos, lineVpos, linelength, 1);
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	/*
	 * Implement dialog resizing on show, with accompanying control repositioning, in a future version...
	 * 
	public short show(XModel xDoc, String title, String message, String rawhexPng, int dlgWidth) {
		return show(xDoc, title, message, rawhexPng, dlgWidth, dialogheight);
	}
	
	public short show(XModel xDoc, String title, String message, String rawhexPng, int dlgWidth, int dlgHeight) {
		XControlModel xDialogModel = m_xDialogControl.getModel();
		XPropertySet xDialogProps = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
		try {
			xDialogProps.setPropertyValue("Width", dlgWidth);
			xDialogProps.setPropertyValue("Height", dlgHeight);
		} catch (Exception e) {
			// nop -- default dimensions will be used
		}
		
		return show(xDoc, title, message, rawhexPng);
	}
	*/
	
	public short show(XModel xDoc, String title, String message, String subtext, int iconIndex) {
		String rawhexPng = null;
		
		switch (iconIndex) {
			case 1:
				// Warning Icon
				rawhexPng = "89504E470D0A1A0A0000000D49484452000000300000003008060000005702F98700000006624B474400FF00FF00FFA0BDA793000000097048597300000B1300000B1301009A9C180000000774494D4507E2040210253784B9A8FA000003504944415468DEED583D6B1B41149C5984255418638C09214508526B42FA5B52A74A9D5F1852A6722D999426FD2D2684608231C6B8087721F8A5D0DE69F7BCF7A1FB100AD1C25923A3BB7B1FF366DF5B60BFF66BA797D86BB0C5818D1FFC5D6A888726B10600A446DF24717457E2D0EE3A30992FACB1724AF23835D1E5506956433D3889A3DF006DD879F62F39200040E200908CF8A3A168A486303E35D1939A4D8DC6104EA8DE8D8FF5751EF79C42B9133FFB76A2D70CA4467F02E5199D2D80BE7FA7691C7DDE590A89C87B800A229642CC8D17E1EA7DC4BB5D74C0528207B0152C923B05802073DC6B41777620892364854BA76E334C0B4424C7D93D7D38A13A721E93F9529E98221248103D9CC4D1F73E9C602FCA63F41F00239175E433EC46BE801F017C9CCC971FBAD8A17A509EBB6CA36A4A218B15D0BDA0473D749BC75EC8575F6EC7B3E549C1D11B00A72E9D481C0538B6150A65D4418836E3D9A2B4E04319717ECFAD512835FAB28C36A5D12AA1536AA2AFDBAC818C3A679EDA886C70BBAF4822386BAB48AD3290C4FA078091BB6179B87CA72E6E6A59D65462F4F53632605B657911A24D5B0A910405CFDB64416D6A7C12AF0A57449C4F1FD72A87E3698ED9AEE5569B156E7441D888D9663984EB0F29CAAF24D65F86C880D83F6F24FFC7BAD72FE24AD516AC3BD52096D79B6441352FDCE88AE074A51AE228480857F2A712939CA6467FEB2D03D91109C157A8A04D5F14B20178D9340BB50EE44724F49F381C8508AC36B75F7DF4425218C81DAA54E1F61472F0B4499FA4EA6533BAF763B80D0A594532FABE8E4AAAA6DFB9207914EA2386A550AE4847A9A99655964D5AE3D9427CEAB82D8F38B75763B2FB99EE78B6406A34435D2E9B731FC5516BCDD730FE05E04A206AA5603269EFC0B2D45E6E64BCD3BB64990861521EC6B3E56121AB0F000E830344035C3633B0C4F85B00276107EA2934992F4B0F013A0C80EE94C7D2224EE3E8BCCCF86DAB907FF12435FABC568584D44DA699EDA8908B01006FAB1CC87E3A959A31BAAE174A4CF418A0CFE3861B5968C89814F7057A9B96D160DDE6D75C850279EB67B9AAA49C08992687154D2824C531B9356DC2383591717BA1EC3533343AA469D20BF9B8136D0258D6B68AB2D1BF77B3CC8A943751215F91A4F78B99CD0521DFAFFDFA1FD75F34D243EF4CDF77560000000049454E44AE426082";
				break;
			case 2:
				// User Error Icon
				rawhexPng = "89504E470D0A1A0A0000000D49484452000000300000003008060000005702F98700000006624B4744000000000000F943BB7F000000097048597300000B1300000B1301009A9C180000000774494D4507E20402102C15801B5257000005334944415468DEC5994D685C5514C77FFF21842C444A08320B17A22E445A41A45D45918AA52E44C13AB82BA188084A4950DF0C5D9441CACC23C45815DD5892084AA82EFC8012BA52F7BA5241A936E0A2B328D9544A1824A78BF7DECC7B33EFE3BED7647A37F367E6BE3BE7DEF3BBE79C77AF28DB3A3603760AA985318B000B7F4BD3721A7507A383F886A676CB9833557A02A281A90D3C346664962EEE5447BC0FD4802FCA9953A675ED04B001D43990663DD0699ABABABF13E8DA0C660D840FAA63060A1F75D1E55A0FF080CB2E38D51C076D20DAA03AC40D73D1A55B1D68038DFDF140D74E806D04C64FB4F580429C948B0D3430F311F5E17EABB2B22E3B3D55F7C03C50264EB55C6CA08D141AAF8AC68F4EBA94AE63F9386932D1A6B207229D899352B1C1FC70F62963EA5E4C2013A72442660DB020DAC4B1B9770815E2A4C9449B7DF1402A4EA26333880610609328660A0DFB0DF41766C7910E65770BC794E27A17B80ADC8FD9B3C3255556FF988E929D2ED7C04E858923C8B038A0120CF40FE8ECAD456E219631FA833F881B1D65E4C880A1DE04CE0267103F23EDA5F4C9D0D4416DA021BAF63BF0B8BBDB098D67D13CBE1B2C5CD73E40BC059A2EA6C8B69016CCE306807C9E025681674A51077FD43066C7FFC9B23795F12BB010371EC09A5A029D0776D23D6600BB60EB71E301CCE3176001E37BCCE2FDF3B5D96C2D2889E31D0A10123F9AC74FA98BECD181119CC6B0513B6E7CECD9BF115F0E7877C4A9367820FE39F0449AE665F9BC9AB93D3C3A601F21FA8931CDAE20B5CCE37AEABAF81C05DE1EF68F3F9BA58D5AD2360784E061CC3E93CFEB999368EA1DD039E026D86DCC3E5F3AA6C5B4950F8D7F1EB335CCE60BB14968105DBB81593DE19AC2E869003791DE348FAF3369F3390B3C022C9BC7BFD92B6F6BA0C3CEEF1743DD0B26905AF3E446A1A85D035A7993F8AFCFC67DD39CCEC1661598AF96D7AC5705A1787B14C8C529C7F8E7AA619344A87C148A27A940CF0117D4E515E7CAC8E749CC56910EBB27AFFD8B42C97E817E00D1916F2F3A187F04B315A427DCA34D5E144A60335AE7A51CF858944446343C087ADAC101C706C6DF75A11A79C00053CAAA6B644FC40AAB316D5F011F1706308F4B181DB07E0A8E2535D4821384F8CBCA6835AAA4560C9DE1D757968E2A33548EE70956401F62F44B1DCF8C69A825E3D3A88F52F641129B5D601D7466E5387F967A43F0780F31AC9DC6F79593BE5B843681764E863D229F17F2CB0E9681FEE41182AD30895DCF0C957011B35575EDA582497C520DA72A0845D88C94C429496A03E338D2634897726B278FA5AA385540C8BE2DC0E6286617835039186F0EE8C8CF4E76A127560E1E21E3FF829238C8B0C9523A9A849F578A03B7AB20B4530A21A9217FFC0C7F5812333F12A9E2AE0F6B277B23E5F973C0F99208EDD482D3086D3B23644C030DF9AC8F6013AE7C4AB24BBA7E0E7421EE89D0F845CC0E9540681B5816DDBD19D06B406778ACE27480D5073E057E084F35E6738BBF710CAE01E78059A083D921F728643D500B6C337EB075327CB1A8E7BE1358E2FB3DB03DD054667F152EC254897B8AC8F8059ADA4A1E2D36B585A985B15D2291D590A6F26BA45C0CA631AB95C346ADC8F8F14B3EB119DD450E3D31520AAA7CBAAF56E7A462D306DB74395E3F09AC81D50B102AD6956F9AB2B129BEE0686A0B2CC4C9B59CAE8450256CE22DE79E589BA1E19DE14BFFA4114AC7A6EC255F12A78921948D8DEB1D59064E134128171B478432705288D38121548C4D3984C670B2B5F07CFE001072C3A61C426338A985B47D000839635301A1448B5CFB2ED8ACCB2D84C319CF0EC63272C326DEEE0099447FCDA94C00930000000049454E44AE426082";
				break;
			case 3:
				// System Error Icon
				rawhexPng = "89504E470D0A1A0A0000000D49484452000000300000003008060000005702F98700000006624B4744000000000000F943BB7F000000097048597300000B1300000B1301009A9C180000000774494D4507E20402102E260DFD51C3000004A54944415468DECD994F681C551CC73FBF21841C4A0D21C81E8416ED41C4168AD4531569B4B31E44A1EDE2AD04111194906091A50759449610628C8ABD589A1494A01EAC423BEB49BDEB493D88D68287E450F6122821487E1EE64DFA32993FEFCDEC461F2C3C66E7EDFCDEEFF799F7FBFE7E2B788E08C614CE0BB41526C45C57206BEE38FA4017F8AA095B3E0B47F01F2D810E70D436326FEE381AC0BB40005CF759289EDE3F0BAC9A070E636C00179BF0DD403710C118D002E681461E2E3531B237F136F0850B4E812B361A63D328C2A52646364E1DE3B0FA1188E0ACC2AA0C0F9B5A38E56EE0168C49ECF9F9BAC6FB2267CD4B710A0A76D6023A83F0BC549F37B40427F98F4E9B81E12459A7CD20B0191042A5380559A7CDA05F5819224EF23FC6C60927492729171C929D2BFC22F0BBC21960DCBABECF9319586C111B711878C613A90D3538050AE74DE268A81FD7B78199A9CDCD4D8105816D870DDBF33560067845E14760478AEFB7E70D89516FC92DF855E031CF97F2B6C06CA87A63174191F705DE0046DDD409D3A1EA3A404FE409852581A73D71FA2D00263C17FD6C1E7EC3BED8549D03DE01FA6A792C15892D8515B58C0708557F129806BED154E48AE6C0446087C711A1EF9BAA3F647D11AA7649E194C646A0D3B48CB7D6FE097C861B427B33B1F809B09722910B795F86AA5D850F934D58BF7953A01DAAFE95B5AE27720A78D3F7980DD4931F858705AEF4445ECDBBA7A9FA167019B8AB704FE1D3237373B36186E78DF1CF01D7144EE3871012C1BA79AB9D34BC15CABBC0EBA1EA9779F7F64466141E11580855FF2EF0FC35E0F12AD9D91B21B97FDF24D0ED15E3B4FCECE6E60325C62F25C657C9D45510B25FFA63C095A800A79143872EE6183F55059BB4BD554EA1F4862605DEEB899C73AEAD454EAAF1BC47F21ACC2924A9FBCDFC41A01B89BC50B6BE27721C5814383108B13752C1E379E32181A71C7EE249DBF8BA23C8332C8F3BC9FF7C0E7C54F6C050F5AA69626D57655FB336208E1A3EE7876E1E999BCB3D2A3336B1087CC0FE64E73F8F60DDA7064845638B5855B6F39254C9FBD0062E55D063BBB5412D848CF19D820C7BBC27F27C91EC0016EAE05407A1480BB44D2472125856588A445E2CD9C4C755710A7C6366B05901A69BF99E9F025615CE083C2A70B5483B8596141FFA29A4F0750936A78065811356D24964C7B992482C326C84807F4A24F192666B9B4960BE483B29DCA302427D4F845A91C8F52C49AC96B6D1ECA4774C6329FE5AC6FACB06239FD10F4C37E28E2B42C0A840AB27B292F6BC186D637F747F676292583B5D48193F2B30EE710ADD011692B6CACB0A5D716CAB98106E039F00DF021D85D39EFF0DFC618A9E09F3EC718F5CB421D0262E5177DB044D5358343CB4D08EC08E1668AA928D6C1B3D16781A3FDD8C3B1BF7179A0B6D8D43E3AA850260A4401F9561309AAE49CAB0116827C667A9D124247B70921A8DDA01F545939662C764FFD2F67A2E4EBE454F8DBF9A72B1A12C1317E1249E9F3A52390F1B1C0B9A24547B703A4884F2B0F18A701AA78342A8089B52848A703A20840AB171452813A7A409362C845CB0A91CE10427F5A8E06408D8782194C649A12D7188078D9033365510B23DBA661E7A491C6A594784FAA6B45CF3B5E75F05821945EDD1A3A30000000049454E44AE426082";
				break;
			default:
				break;
		}
		
		return show(xDoc, title, message, subtext, rawhexPng);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, String rawhexPng) {
		// Use MessageBoxType.ERRORBOX for a System Error
		// Use MessageBoxType.INFOBOX for a User Error
		// Use MessageBoxType.WARNINGBOX for a Warning
		
		
		// Configure Warning Text to the current Application Font and at size 12pt and BOLD
		//// Get Label XPropertySet interface
		XControl xControl = UnoRuntime.queryInterface(XControl.class, guiLabel);
		XControlModel xControlModel = xControl.getModel();
		XPropertySet xLabelProps = UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
		
		//// Get FontDescriptor for Application Font
		XStyleSettingsSupplier xStyleSettingsSupplier = UnoRuntime.queryInterface(XStyleSettingsSupplier.class, xDoc.getCurrentController().getFrame().getContainerWindow());
		XStyleSettings xStyleSettings = xStyleSettingsSupplier.getStyleSettings();
		FontDescriptor appFontDescriptor = xStyleSettings.getApplicationFont();
		appFontDescriptor.Height = 12;
		appFontDescriptor.Weight = FontWeight.BOLD;		
		
		xControl = UnoRuntime.queryInterface(XControl.class, guiLabel2);
		xControlModel = xControl.getModel();
		XPropertySet xLabel2Props = UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
		
		XPropertySet xIconProps = null;
		XGraphic 	 xGraphic	= null;
		
		// Configure Icon
		if ( rawhexPng != null ) {
			byte[] hexbinaryIcon = DatatypeConverter.parseHexBinary(rawhexPng);
			
			// If getGraphic throws, just continue; the default Warning Icon will be used.
			try {
				xGraphic = getGraphic(hexbinaryIcon, "png");
				
				//// Get Label XPropertySet interface
				XControl xIconControl = UnoRuntime.queryInterface(XControl.class, guiIcon);
				XControlModel xIconControlModel = xIconControl.getModel();
				xIconProps = UnoRuntime.queryInterface(XPropertySet.class, xIconControlModel);
			} catch (Exception e) {
				// nop
			}
		}
		
		try {
			if (xIconProps != null)
				xIconProps.setPropertyValue("Graphic", xGraphic);
			
			xLabelProps.setPropertyValue("Label", message);
			xLabelProps.setPropertyValue("FontDescriptor", appFontDescriptor);
			
			appFontDescriptor.Height = 10;
			appFontDescriptor.Weight = FontWeight.NORMAL;
			xLabel2Props.setPropertyValue("Label", subtext);
			xLabel2Props.setPropertyValue("FontDescriptor", appFontDescriptor);
		} catch (Exception e) {
			System.out.println("Oh no! Some property is wrong!");
			e.printStackTrace(System.err);
		}
		
		return super.show(xDoc, title);
	}
}
